package org.simple.clinic.bppassportgen.approvals.verifyv2

import org.junit.Test
import org.simple.bppassportgen.BLACK
import org.simple.bppassportgen.FONT_ID
import org.simple.bppassportgen.RenderableSpec
import org.simple.bppassportgen.renderable.Renderable.Type
import org.simple.bppassportgen.renderable.qrcode.BarcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderSpec
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase
import org.simple.clinic.bppassportgen.util.readUuids
import org.simple.clinic.bppassportgen.util.resourceFilePath

class VerifyGenerationOfPdf : VerifyTestBase(
    templateFilePath = resourceFilePath("blank.pdf"),
    pageCount = 2,
    rowCount = 2,
    columnCount = 2,
    renderSpecs = listOf(
        RenderableSpec(
            pageNumber = 0,
            type = Type.PassportShortcode,
            spec = ShortcodeRenderSpec(
                positionX = 80F,
                positionY = 115F,
                fontSize = 25F,
                characterSpacing = 1.3F,
                color = BLACK,
                fontId = FONT_ID
            )
        ),
        RenderableSpec(
            pageNumber = 1,
            type = Type.PassportQrCode,
            spec = BarcodeRenderSpec(
                width = 120,
                height = 120,
                matrixScale = 2F,
                positionX = 10F,
                positionY = 10F,
                color = BLACK
            )
        )
    )
) {

  @Test
  fun `verify generation of PDFs`() {
    app.run(readUuids("uuids_pdfs.txt"))

    runApprovals(8) { pdfNumber, pageNumber: Int -> "bp pdf $pdfNumber.$pageNumber" }
  }
}
