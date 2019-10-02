package org.simple.bppassportgen

data class BarcodeRenderSpec(
    val width: Int,
    val height: Int,
    val matrixScale: Float,
    val positionX: Float,
    val positionY: Float
)
