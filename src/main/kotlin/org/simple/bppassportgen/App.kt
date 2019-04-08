package org.simple.bppassportgen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import java.io.File
import java.util.UUID
import java.util.logging.Logger

fun main() {
  App().run()
}

class App {

  val logger = Logger.getLogger("App")

  val uuid = UUID.fromString("89dd227d-8c78-4310-9e1f-5cf5e67de2d3")
  val shortCode = "892 2787"

  val foregroundColor = PDColor(
      floatArrayOf(0F, 0F, 0F, 1F),
      COSName.DEVICECMYK,
      PDDeviceCMYK.INSTANCE
  )

  fun run() {
    val qrCodeWriter = QRCodeWriter()
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
        EncodeHintType.MARGIN to 0
    )

    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, 80, 80, hints)
    val bitMatrixRenderable = BitMatrixRenderable(bitMatrix)

    val pdfInput = File("./bp_passport_template.pdf")
    val pdfOutput = File("./bp_passport_out.pdf")
    val fontPath = File("Metropolis-Medium.ttf")

    PDDocument.load(pdfInput).use { document ->
      val page = document.getPage(0)
      val font = PDType0Font.load(document, fontPath)

      PDPageContentStream(
          document,
          page,
          PDPageContentStream.AppendMode.APPEND,
          false
      ).use { contentStream ->
        contentStream.beginText()
        contentStream.setNonStrokingColor(0F, 0F, 0F, 1F)
        contentStream.newLineAtOffset(200F, 220F)
        contentStream.setCharacterSpacing(2.5F)
        contentStream.setFont(font, 12F)
        contentStream.showText(shortCode)
        contentStream.endText()

        bitMatrixRenderable.render(
            contentStream,
            280F,
            150F,
            drawBackground = false,
            applyForegroundColor = { it.setStrokingColor(foregroundColor) }
        )
      }

      document.save(pdfOutput)
    }
  }
}
