package org.simple.bppassportgen

import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger

fun main() {
  App().run()
}

class App {

  val logger = Logger.getLogger("App")

  fun run() {
    val computationThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val ioThreadPool = Executors.newCachedThreadPool()

    val numberOfPassports = 5

    val outDirectory = File("./out")
    outDirectory.mkdirs()

    val blackCmyk = PDColor(
        floatArrayOf(0F, 0F, 0F, 1F),
        COSName.DEVICECMYK,
        PDDeviceCMYK.INSTANCE
    )

    val pdfInputBytes = File("./bp_passport_template.pdf").readBytes()
    val fontInputBytes = File("./Metropolis-Medium.ttf").readBytes()

    val uuids = (0 until numberOfPassports)
        .map { UUID.randomUUID() }
        .distinct()

    val qrCodeWriter = QRCodeWriter()
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
        EncodeHintType.MARGIN to 0
    )

    val generatingPdfTasks = mutableMapOf<UUID, Future<PDDocument>>()
    val savePdfTasks = mutableListOf<Future<Any>>()

    uuids
        .map { uuid ->
          GenerateBpPassportTask(
              pdfBytes = pdfInputBytes,
              fontBytes = fontInputBytes,
              uuid = uuid,
              qrCodeWriter = qrCodeWriter,
              hints = hints,
              shortCodeColor = blackCmyk,
              barcodeColor = blackCmyk
          )
        }
        .forEach { task ->
          generatingPdfTasks[task.uuid] = computationThreadPool.submit(task)
        }

    var tasksComplete = false
    while (tasksComplete.not()) {
      Thread.sleep(1000L)
      val generated = generatingPdfTasks.filter { (_, future) -> future.isDone }

      generated
          .map { it.key to it.value.get() }
          .map { (uuid, document) ->
            SaveBpPassportTask(
                document = document,
                uuid = uuid,
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
      println("Finished $numberOfSavedPdfs/${uuids.size} Passports!")

      if (savePdfTasks.size == uuids.size && savePdfTasks.all { it.isDone }) {
        tasksComplete = true
      }
    }

    computationThreadPool.shutdown()
    ioThreadPool.shutdown()
  }
}
