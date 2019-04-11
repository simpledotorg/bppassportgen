package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.Closeable

class Output(val source: PDDocument, val final: PDDocument) : Closeable {

  override fun close() {
    source.close()
    final.close()
  }
}
