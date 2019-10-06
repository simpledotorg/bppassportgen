package org.simple.bppassportgen.renderable.qrcode

data class BarcodeRenderSpec(
    val width: Int,
    val height: Int,
    val matrixScale: Float,
    val positionX: Float,
    val positionY: Float,
    val colorId: String
)
