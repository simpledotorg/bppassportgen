package org.simple.bppassportgen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.concurrent.Callable

class GenerateBpPassportTask(
    val pdfBytes: ByteArray,
    val fontBytes: ByteArray,
    val uuid: UUID,
    val qrCodeWriter: QRCodeWriter,
    val hints: Map<EncodeHintType, Any>,
    val shortCodeColor: PDColor,
    val barcodeColor: PDColor
) : Callable<PDDocument> {

  override fun call(): PDDocument {
    val shortCode = uuid
        .toString()
        .filter { it.isDigit() }
        .take(7)
        .let { shortCode ->
          val prefix = shortCode.substring(0, 3)
          val suffix = shortCode.substring(3)

          "$prefix $suffix"
        }

    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, 80, 80, hints)
    val bitMatrixRenderable = BitMatrixRenderable(bitMatrix)

    val document = PDDocument.load(pdfBytes)
    val page = document.getPage(0)
    val font = PDType0Font.load(document, ByteArrayInputStream(fontBytes))

    PDPageContentStream(
        document,
        page,
        PDPageContentStream.AppendMode.APPEND,
        false
    ).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(shortCodeColor)
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
          applyForegroundColor = { it.setStrokingColor(barcodeColor) }
      )
    }

    return document
  }
}
