package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

data class BarcodeRenderSpec(
    val width: Int,
    val height: Int,
    val matrixScale: Float,
    val positionX: Float,
    val positionY: Float,
    val color: PDColor
)
