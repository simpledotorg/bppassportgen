package org.simple.bppassportgen.renderable

import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.OpenedDocument

interface Renderable {
  fun render(document: OpenedDocument, page: PDPage)

  enum class Type {
    PassportQrCode,
    PassportShortcode
  }
}
