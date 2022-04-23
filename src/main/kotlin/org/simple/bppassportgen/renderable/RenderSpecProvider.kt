package org.simple.bppassportgen.renderable

import org.simple.bppassportgen.RenderableSpec
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.renderable.qrcode.BarcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderSpec

interface RenderSpecProvider {
  fun renderSpecs(generatorType: GeneratorType): List<RenderableSpec>
}

class RenderSpecProviderImpl : RenderSpecProvider {

  private val metropolisFontId = "Metropolis-Medium"
  private val blackCmykId = "cmyk_black"

  override fun renderSpecs(generatorType: GeneratorType): List<RenderableSpec> {
    val barcodeRenderSpec = when (generatorType) {
      GeneratorType.Passport -> BarcodeRenderSpec(
        width = 80,
        height = 80,
        matrixScale = 1.35F,
        positionX = 196F,
        positionY = 107.5F,
        colorId = blackCmykId
      )
      GeneratorType.Sticker -> BarcodeRenderSpec(
        width = 80,
        height = 80,
        matrixScale = 0.85F,
        positionX = 4.5F,
        positionY = 17F,
        colorId = blackCmykId
      )
    }

    val shortcodeRenderSpec = when (generatorType) {
      GeneratorType.Passport -> ShortcodeRenderSpec(
        positionX = 88F,
        positionY = 210F,
        fontSize = 12F,
        characterSpacing = 2.4F,
        fontId = metropolisFontId,
        colorId = blackCmykId
      )
      GeneratorType.Sticker -> ShortcodeRenderSpec(
        positionX = 16F,
        positionY = 8F,
        fontSize = 8F,
        characterSpacing = 1.2F,
        fontId = metropolisFontId,
        colorId = blackCmykId
      )
    }

    return listOf(
      RenderableSpec(0, Renderable.Type.PassportQrCode, barcodeRenderSpec),
      RenderableSpec(0, Renderable.Type.PassportShortcode, shortcodeRenderSpec)
    )
  }
}
