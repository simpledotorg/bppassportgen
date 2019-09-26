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

      App().run(
          uuidsToGenerate = uuidsToGenerate,
          templateFilePath = templateFilePath,
          outDirectory = outDirectory,
          pageCount = pageCount,
          rowCount = rowCount,
          columnCount = columnCount,
          isSticker = isSticker
      )
    }
  }
}

class App(
    private val computationThreadPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
    private val ioThreadPool: ExecutorService = Executors.newCachedThreadPool(),
    private val progressPoll: ProgressPoll = RealProgressPoll(Duration.ofSeconds(1))
) {

  fun run(
      uuidsToGenerate: List<UUID>,
      templateFilePath: String,
      outDirectory: File,
      pageCount: Int,
      rowCount: Int,
      columnCount: Int,
      isSticker: Boolean
  ) {

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

          val task = createPassportGenerationTask(isSticker, index, pdfInputBytes, fontInputBytes, uuidBatch, qrCodeWriter, hints, blackCmyk, rowCount, columnCount)

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
      println("Finished $numberOfSavedPdfs/${uuidBatches.size} Passports!")

      if (savePdfTasks.size == uuidBatches.size && savePdfTasks.all { it.isDone }) {
        tasksComplete = true
      }
      waitForProgress()
    }

    computationThreadPool.shutdown()
    ioThreadPool.shutdown()
  }

  private fun waitForProgress() {
    progressPoll.poll()
  }

  private fun createPassportGenerationTask(
      isSticker: Boolean,
      index: Int,
      pdfInputBytes: ByteArray,
      fontInputBytes: ByteArray,
      uuidBatch: List<List<UUID>>,
      qrCodeWriter: QRCodeWriter,
      hints: Map<EncodeHintType, Any>,
      blackCmyk: PDColor,
      rowCount: Int,
      columnCount: Int
  ): Callable<Output> {
    return if (isSticker) {
      GenerateBpStickerTask(
          taskNumber = index + 1,
          pdfBytes = pdfInputBytes,
          fontBytes = fontInputBytes,
          uuidBatches = uuidBatch,
          qrCodeWriter = qrCodeWriter,
          hints = hints,
          shortCodeColor = blackCmyk,
          barcodeColor = blackCmyk,
          rowCount = rowCount,
          columnCount = columnCount
      )
    } else {
      GenerateBpPassportTask(
          taskNumber = index + 1,
          pdfBytes = pdfInputBytes,
          fontBytes = fontInputBytes,
          uuidBatches = uuidBatch,
          qrCodeWriter = qrCodeWriter,
          hints = hints,
          shortCodeColor = blackCmyk,
          barcodeColor = blackCmyk,
          rowCount = rowCount,
          columnCount = columnCount
      )
    }
  }
}
