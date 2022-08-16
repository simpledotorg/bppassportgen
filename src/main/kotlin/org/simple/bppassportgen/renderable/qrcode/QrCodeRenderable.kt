package org.simple.bppassportgen.renderable.qrcode

import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.ColorProvider
import org.simple.bppassportgen.OpenedDocument
import org.simple.bppassportgen.PdfUtil
import org.simple.bppassportgen.qrcodegen.QrCodeGenerator
import org.simple.bppassportgen.renderable.Renderable
import java.util.UUID

class QrCodeRenderable(
  private val qrCodeGenerator: QrCodeGenerator,
  private val uuid: UUID,
  private val spec: BarcodeRenderSpec,
  private val colorProvider: ColorProvider
) : Renderable {

  override fun render(document: OpenedDocument, page: PDPage) {
    val bitMatrix = qrCodeGenerator.generateQrCode(uuid.toString(), spec.width, spec.height)
    val renderBitMatrix = RenderBitMatrixOnPdContentStream(bitMatrix, matrixScale = spec.matrixScale)

    PdfUtil.streamForPage(document.document, page).use { contentStream ->

      renderBitMatrix.render(
        contentStream,
        spec.positionX,
        spec.positionY,
        applyForegroundColor = { it.setStrokingColor(colorProvider.colorById(spec.colorId)) }
      )
    }
  }
}
