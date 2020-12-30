package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream
import java.io.File

class PdDocumentFactory(private val fontsToLoad: Map<String, ByteArray>) {

  fun emptyDocument(): OpenedDocument {
    val document = PDDocument()

    val loadedFonts = fontsToLoad
        .mapValues { (_, fontBytes) -> PDType0Font.load(document, ByteArrayInputStream(fontBytes)) }

    return OpenedDocument(document, loadedFonts)
  }
}
