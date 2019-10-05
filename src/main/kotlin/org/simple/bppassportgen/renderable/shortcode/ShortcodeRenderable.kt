package org.simple.bppassportgen.renderable.shortcode

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.PdfUtil
import org.simple.bppassportgen.renderable.Renderable
import java.util.UUID

class ShortcodeRenderable(
    private val uuid: UUID,
    private val spec: ShortcodeRenderSpec
): Renderable {

  override fun render(document: PDDocument, page: PDPage) {
    val shortCode = shortCodeForUuid(uuid)
    PdfUtil.streamForPage(document, page).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(spec.color)
      contentStream.newLineAtOffset(spec.positionX, spec.positionY)
      contentStream.setCharacterSpacing(spec.characterSpacing)
      contentStream.setFont(spec.font, spec.fontSize)
      contentStream.showText(shortCode)
      contentStream.endText()
    }
  }

  private fun shortCodeForUuid(uuid: UUID): String {
    return uuid
        .toString()
        .filter { it.isDigit() }
        .take(7)
        .let { shortCode ->
          val prefix = shortCode.substring(0, 3)
          val suffix = shortCode.substring(3)

          "$prefix $suffix"
        }
  }
}
