package org.simple.bppassportgen

import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.simple.bppassportgen.consoleprinter.ConsolePrinter
import org.simple.bppassportgen.consoleprinter.RealConsolePrinter
import org.simple.bppassportgen.progresspoll.ProgressPoll
import org.simple.bppassportgen.progresspoll.RealProgressPoll
import java.io.File
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

fun main(args: Array<String>) {
  val options = Options()
      .apply {
        addRequiredOption("c", "count", true, "Number of BP Passports to generate")
        addRequiredOption("t", "template", true, "Path to the template file")
        addOption("o", "output", true, "Directory to save the generated BP passports")
        addOption("p", "pages", true, "Number of pages in a passport")
        addOption("rc", "row-count", true, "Number of rows in a page")
        addOption("cc", "column-count", true, "Number of columns in a page")
        addOption("h", "help", false, "Print this message")
        addOption("sticker", false, "Generate stickers instead of the BP Passports")
      }

  val helpFormatter = HelpFormatter()

  if (args.isEmpty()) {
    helpFormatter.printHelp("bppassportgen", options)
  } else {

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    if (cmd.hasOption("h")) {
      helpFormatter.printHelp("bppassportgen", options)
    } else {
      val numberOfPassports = cmd.getOptionValue("c").toInt()
      val templateFilePath = cmd.getOptionValue("t")
      val outDirectory = File(cmd.getOptionValue("o", "./out"))
      val pageCount = cmd.getOptionValue("p", "1").toInt()
      val rowCount = cmd.getOptionValue("rc", "1").toInt()
      val columnCount = cmd.getOptionValue("cc", "1").toInt()
      val isSticker = cmd.hasOption("sticker")

      require(numberOfPassports > 0) { "Number of passports must be > 0!" }
      require(rowCount * columnCount <= numberOfPassports) { "row count * column count of passports must be <= count!" }

      val uuidsToGenerate = (0 until numberOfPassports).map { UUID.randomUUID() }

      App(
          templateFilePath = templateFilePath,
          outDirectory = outDirectory,
          pageCount = pageCount,
          rowCount = rowCount,
          columnCount = columnCount,
          isSticker = isSticker
      ).run(uuidsToGenerate = uuidsToGenerate)
    }
  }
}

class App(
    private val computationThreadPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
    private val ioThreadPool: ExecutorService = Executors.newCachedThreadPool(),
    private val progressPoll: ProgressPoll = RealProgressPoll(Duration.ofSeconds(1)),
    private val consolePrinter: ConsolePrinter = RealConsolePrinter(),
    private val templateFilePath: String,
    private val outDirectory: File,
    private val pageCount: Int,
    private val rowCount: Int,
    private val columnCount: Int,
    private val isSticker: Boolean
) {

  fun run(uuidsToGenerate: List<UUID>) {

    val mergeCount = rowCount * columnCount

    outDirectory.mkdirs()

    val blackCmyk = PDColor(
        floatArrayOf(0F, 0F, 0F, 1F),
        COSName.DEVICECMYK,
        PDDeviceCMYK.INSTANCE
    )

    val pdfInputBytes = File(templateFilePath).readBytes()
    val fontInputBytes = javaClass.getResourceAsStream("/Metropolis-Medium.ttf").readBytes()

    val uuidBatches = uuidsToGenerate
        .distinct()
        .windowed(size = mergeCount, step = mergeCount)
        .windowed(size = pageCount, step = pageCount)

    val qrCodeWriter = QRCodeWriter()
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
        EncodeHintType.MARGIN to 0
    )

    val generatingPdfTasks = mutableMapOf<Int, Future<Output>>()
    val savePdfTasks = mutableListOf<Future<Any>>()

    uuidBatches
        .mapIndexed { index, uuidBatch ->

          val task = createPassportGenerationTask(isSticker, pdfInputBytes, fontInputBytes, uuidBatch, qrCodeWriter, hints, blackCmyk, rowCount, columnCount)

          task to index + 1
        }
        .forEach { (task, taskNumber) ->
          generatingPdfTasks[taskNumber] = computationThreadPool.submit(task)
        }

    var tasksComplete = false
    while (tasksComplete.not()) {
      val generated = generatingPdfTasks.filter { (_, future) -> future.isDone }

      generated
          .map { it.key to it.value.get() }
          .map { (taskNumber, output) ->
            SaveBpPassportTask(
                output = output,
                taskNumber = taskNumber,
                totalSize = uuidBatches.size,
                directory = outDirectory
            )
          }
          .forEach {
            savePdfTasks += ioThreadPool.submit(it, Any())
          }

      generated
          .map { it.key }
          .forEach { generatingPdfTasks.remove(it) }

      val numberOfSavedPdfs = savePdfTasks.count { it.isDone }
      consolePrinter.print("Finished $numberOfSavedPdfs/${uuidBatches.size} Passports!")

      if (savePdfTasks.size == uuidBatches.size && savePdfTasks.all { it.isDone }) {
        tasksComplete = true
      }
      progressPoll.poll()
    }

    computationThreadPool.shutdown()
    ioThreadPool.shutdown()
  }

  private fun createPassportGenerationTask(
      isSticker: Boolean,
      pdfInputBytes: ByteArray,
      fontInputBytes: ByteArray,
      uuidBatch: List<List<UUID>>,
      qrCodeWriter: QRCodeWriter,
      hints: Map<EncodeHintType, Any>,
      blackCmyk: PDColor,
      rowCount: Int,
      columnCount: Int
  ): Callable<Output> {
    val barcodeRenderSpec = if (isSticker) {
      BarcodeRenderSpec(width = 80, height = 80, matrixScale = 0.85F, positionX = 4.5F, positionY = 17F)
    } else {
      BarcodeRenderSpec(width = 80, height = 80, matrixScale = 1.35F, positionX = 196F, positionY = 107.5F)
    }

    val shortcodeRenderSpec = if (isSticker) {
      ShortcodeRenderSpec(positionX = 16F, positionY = 8F, fontSize = 8F, characterSpacing = 1.2F)
    } else {
      ShortcodeRenderSpec(positionX = 72.5F, positionY = 210F, fontSize = 12F, characterSpacing = 2.4F)
    }

    return GenerateBpPassportTask(
        pdfBytes = pdfInputBytes,
        fontBytes = fontInputBytes,
        uuidsGroupedByPage = uuidBatch,
        qrCodeWriter = qrCodeWriter,
        hints = hints,
        shortCodeColor = blackCmyk,
        barcodeColor = blackCmyk,
        rowCount = rowCount,
        columnCount = columnCount,
        barcodeRenderSpec = barcodeRenderSpec,
        shortcodeRenderSpec = shortcodeRenderSpec,
        templatePageIndexToRenderCode = 0
    )
  }
}
