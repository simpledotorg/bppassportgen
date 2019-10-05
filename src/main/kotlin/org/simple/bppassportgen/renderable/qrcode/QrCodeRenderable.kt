package org.simple.bppassportgen.renderable.qrcode

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.PdfUtil
import org.simple.bppassportgen.qrcodegen.QrCodeGenerator
import java.util.UUID

class QrCodeRenderable {

  fun render(
      qrCodeGenerator: QrCodeGenerator,
      uuid: UUID,
      document: PDDocument,
      page: PDPage,
      spec: BarcodeRenderSpec
  ) {
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
