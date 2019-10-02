package org.simple.bppassportgen

import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.pdmodel.PDPage

object PdfUtil {

  fun clone(pdPage: PDPage): PDPage {
    val pageDictionary = pdPage.cosObject
    val clonedDictionary = COSDictionary(pageDictionary)

    return PDPage(clonedDictionary)
  }
}
