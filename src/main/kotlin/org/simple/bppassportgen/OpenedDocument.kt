package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font

data class OpenedDocument(
  val document: PDDocument,
  private val loadedFonts: Map<String, PDType0Font>
) {

  fun fontById(fontId: String): PDType0Font = loadedFonts.getValue(fontId)
}
