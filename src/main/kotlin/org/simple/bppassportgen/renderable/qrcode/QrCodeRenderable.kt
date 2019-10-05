package org.simple.bppassportgen.renderable.qrcode

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.PdfUtil
import org.simple.bppassportgen.qrcodegen.QrCodeGenerator
import java.util.UUID

class QrCodeRenderable(
    private val qrCodeGenerator: QrCodeGenerator,
    private val uuid: UUID,
    private val spec: BarcodeRenderSpec
) {

  fun render(document: PDDocument, page: PDPage) {
    val bitMatrix = qrCodeGenerator.generateQrCode(uuid.toString(), spec.width, spec.height)
    val renderBitMatrix = RenderBitMatrixOnPdContentStream(bitMatrix, matrixScale = spec.matrixScale)

    PdfUtil.streamForPage(document, page).use { contentStream ->

      renderBitMatrix.render(
          contentStream,
          spec.positionX,
          spec.positionY,
          applyForegroundColor = { it.setStrokingColor(spec.color) }
      )
    }
  }

}
