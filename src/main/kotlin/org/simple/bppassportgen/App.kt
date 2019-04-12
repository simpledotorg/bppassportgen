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
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger

fun main(args: Array<String>) {
  val options = Options()
      .apply {
        addRequiredOption("c", "count", true, "Number of BP Passports to generate")
        addOption("o", "output", true, "Directory to save the generated BP passports")
        addOption("p", "pages", true, "Number of pages in a passport")
        addOption("rc", "row-count", true, "Number of rows in a page")
        addOption("cc", "column-count", true, "Number of columns in a page")
        addOption("h", "help", false, "Print this message")
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
      val outDirectory = File(cmd.getOptionValue("o", "./out"))
      val pageCount = cmd.getOptionValue("p", "1").toInt()
      val rowCount = cmd.getOptionValue("rc", "1").toInt()
      val columnCount = cmd.getOptionValue("cc", "1").toInt()

      App().run(
          numberOfPassports = numberOfPassports,
          outDirectory = outDirectory,
          pageCount = pageCount,
          rowCount = rowCount,
          columnCount = columnCount
      )
    }
  }
}

class App {

  val logger = Logger.getLogger("App")

  fun run(
      numberOfPassports: Int,
      outDirectory: File,
      pageCount: Int,
      rowCount: Int,
      columnCount: Int
  ) {
    if (numberOfPassports <= 0) {
      throw IllegalArgumentException("Number of passports must be > 0!")
    }

    if (rowCount * columnCount > numberOfPassports) {
      throw IllegalArgumentException("row count * column count of passports must be > count!")
    }

    val mergeCount = rowCount * columnCount

    val computationThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val ioThreadPool = Executors.newCachedThreadPool()

    outDirectory.mkdirs()

    val blackCmyk = PDColor(
        floatArrayOf(0F, 0F, 0F, 1F),
        COSName.DEVICECMYK,
        PDDeviceCMYK.INSTANCE
    )

    val pdfInputBytes = javaClass.getResourceAsStream("/bp_passport_template.pdf").readBytes()
    val fontInputBytes = javaClass.getResourceAsStream("/Metropolis-Medium.ttf").readBytes()

    val uuidBatches = (0 until numberOfPassports)
        .map { UUID.randomUUID() }
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
        .forEach { task ->
          generatingPdfTasks[task.taskNumber] = computationThreadPool.submit(task)
        }

    var tasksComplete = false
    while (tasksComplete.not()) {
      Thread.sleep(1000L)
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
    }

    computationThreadPool.shutdown()
    ioThreadPool.shutdown()
  }
}
