package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

data class ShortcodeRenderSpec(
    val positionX: Float,
    val positionY: Float,
    val fontSize: Float,
    val characterSpacing: Float,
    val color: PDColor
)
