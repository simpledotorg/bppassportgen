package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream
import java.io.File

class PdDocumentFactory(fontsToLoad: Map<String, File>) {

  private val loadedFontBytes: Map<String, ByteArray>

  init {
    loadedFontBytes = fontsToLoad.mapValues { (_, fontFile) -> fontFile.readBytes() }
  }

  fun emptyDocument(): OpenedDocument {
    val document = PDDocument()

    val loadedFonts = loadedFontBytes
        .mapValues { (_, fontBytes) -> PDType0Font.load(document, ByteArrayInputStream(fontBytes)) }

    return OpenedDocument(document, loadedFonts)
  }
}
