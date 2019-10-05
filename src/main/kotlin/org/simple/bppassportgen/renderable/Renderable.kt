package org.simple.bppassportgen.renderable

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

interface Renderable {
  fun render(document: PDDocument, page: PDPage)
}
