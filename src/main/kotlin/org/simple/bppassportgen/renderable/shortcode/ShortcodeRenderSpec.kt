package org.simple.bppassportgen.renderable.shortcode

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

data class ShortcodeRenderSpec(
    val positionX: Float,
    val positionY: Float,
    val fontSize: Float,
    val characterSpacing: Float,
    val color: PDColor,
    val fontId: String
)
