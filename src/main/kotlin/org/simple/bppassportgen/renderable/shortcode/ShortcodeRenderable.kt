package org.simple.bppassportgen.renderable.shortcode

import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.ColorProvider
import org.simple.bppassportgen.OpenedDocument
import org.simple.bppassportgen.PdfUtil
import org.simple.bppassportgen.renderable.Renderable
import java.util.UUID

class ShortcodeRenderable(
    private val uuid: UUID,
    private val spec: ShortcodeRenderSpec,
    private val colorProvider: ColorProvider
): Renderable {

  override fun render(document: OpenedDocument, page: PDPage) {
    val shortCode = shortCodeForUuid(uuid)
    PdfUtil.streamForPage(document.document, page).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(colorProvider.colorById(spec.colorId))
      contentStream.newLineAtOffset(spec.positionX, spec.positionY)
      contentStream.setCharacterSpacing(spec.characterSpacing)
      contentStream.setFont(document.fontById(spec.fontId), spec.fontSize)
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
