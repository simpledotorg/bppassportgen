package org.simple.clinic.bppassportgen.approvals.verifypdfs

import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.junit.Test
import org.simple.bppassportgen.RenderableSpec
import org.simple.bppassportgen.renderable.Renderable.Type
import org.simple.bppassportgen.renderable.qrcode.BarcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderSpec
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase
import org.simple.clinic.bppassportgen.util.readUuids
import org.simple.clinic.bppassportgen.util.resourceFilePath

private const val FONT_ID = "Pacifico"
private val RED = PDColor(
    floatArrayOf(0F, 1F, 1F, 0F),
    COSName.DEVICECMYK,
    PDDeviceCMYK.INSTANCE
)
private val BLUE = PDColor(
    floatArrayOf(1F, 1F, 0F, 0F),
    COSName.DEVICECMYK,
    PDDeviceCMYK.INSTANCE
)
private const val RED_CMYK = "red_cmyk"
private const val BLUE_CMYK = "blue_cmyk"

class VerifyGenerationOfPdf : VerifyTestBase(
    templateFilePath = resourceFilePath("blank.pdf"),
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
                fontId = FONT_ID,
                colorId = RED_CMYK
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
                colorId = BLUE_CMYK
            )
        )
    ),
    fonts = mapOf(
        FONT_ID to "Pacifico-Regular.ttf"
    ),
    colors = mapOf(
        BLUE_CMYK to BLUE,
        RED_CMYK to RED
    )
) {

  @Test
  fun `verify generation of PDFs`() {
    app.run(readUuids("uuids_pdfs.txt"))

    runApprovals(8) { pdfNumber, pageNumber: Int -> "bp pdf $pdfNumber.$pageNumber" }
  }
}
