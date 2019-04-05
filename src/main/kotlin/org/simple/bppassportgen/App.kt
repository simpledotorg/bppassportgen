package org.simple.bppassportgen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.nio.file.FileSystems
import java.util.UUID
import java.util.logging.Logger
import javax.imageio.ImageIO

fun main() {
  App().run()
}

class App {

  val logger = Logger.getLogger("App")

  val uuid = UUID.fromString("89dd227d-8c78-4310-9e1f-5cf5e67de2d3")
  val shortCode = "892 2787"

  val black = 0xFF000000.toInt()
  val transparent = 0x00FFFFFF.toInt()

  fun run() {
    logger.info(ImageIO.getWriterFormatNames().joinToString())
    val qrCodeWriter = QRCodeWriter()
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
        EncodeHintType.MARGIN to 0
    )

    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, 256, 256, hints)
    val imagePath = FileSystems.getDefault().getPath("./barcode.png")
    //    MatrixToImageWriter.writeToPath(
    //        bitMatrix,
    //        "PNG",
    //        imagePath,
    //        MatrixToImageConfig(black, transparent)
    //    )

    val pdfInput = File("./bp_passport_template.pdf")
    val pdfOutput = File("./bp_passport_out.pdf")
    val fontPath = File("Metropolis-Medium.ttf")

    logger.info(imagePath.toString())
    PDDocument.load(pdfInput).use { document ->
      val page = document.getPage(0)
      val image = PDImageXObject.createFromFile(imagePath.toString(), document)
      val font = PDType0Font.load(document, fontPath)

      PDPageContentStream(
          document,
          page,
          PDPageContentStream.AppendMode.APPEND,
          false
      ).use { contentStream ->
        contentStream.drawImage(image, 280F, 150F, 100F, 100F)

        contentStream.beginText()
        contentStream.setNonStrokingColor(0F, 0F, 0F, 1F)
        contentStream.newLineAtOffset(200F, 220F)
        contentStream.setCharacterSpacing(2.5F)
        contentStream.setFont(font, 12F)
        contentStream.showText(shortCode)

        contentStream.endText()
      }

      document.save(pdfOutput)
    }
  }
}
