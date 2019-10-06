package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

class ColorProvider(private val colorMap: Map<String, PDColor>) {

  fun colorById(colorId: String): PDColor = colorMap.getValue(colorId)
}
