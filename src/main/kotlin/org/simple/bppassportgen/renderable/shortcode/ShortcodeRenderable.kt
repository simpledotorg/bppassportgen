package org.simple.bppassportgen.renderable.shortcode

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.simple.bppassportgen.PdfUtil
import java.util.UUID

class ShortcodeRenderable {

  fun render(
      uuid: UUID,
      document: PDDocument,
      page: PDPage,
      font: PDType0Font,
      spec: ShortcodeRenderSpec
  ) {
    val shortCode = shortCodeForUuid(uuid)
    PdfUtil.streamForPage(document, page).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(spec.color)
      contentStream.newLineAtOffset(spec.positionX, spec.positionY)
      contentStream.setCharacterSpacing(spec.characterSpacing)
      contentStream.setFont(font, spec.fontSize)
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
