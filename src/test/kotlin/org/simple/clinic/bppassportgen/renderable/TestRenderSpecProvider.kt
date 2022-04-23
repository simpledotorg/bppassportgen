package org.simple.clinic.bppassportgen.renderable

import org.simple.bppassportgen.RenderableSpec
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.renderable.RenderSpecProvider
import org.simple.bppassportgen.renderable.Renderable
import org.simple.bppassportgen.renderable.qrcode.BarcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderSpec

class TestRenderSpecProvider : RenderSpecProvider {

  companion object {
    private const val FONT_ID = "Pacifico"
    private const val RED_CMYK = "red_cmyk"
    private const val BLUE_CMYK = "blue_cmyk"
  }

  override fun renderSpecs(generatorType: GeneratorType): List<RenderableSpec> {
    return listOf(
      RenderableSpec(
        pageNumber = 0,
        type = Renderable.Type.PassportShortcode,
        spec = ShortcodeRenderSpec(
          positionX = 80F,
          positionY = 115F,
          fontSize = 25F,
          characterSpacing = 1.3F,
          fontId = FONT_ID,
          colorId = RED_CMYK
        )
      ),
      RenderableSpec(
        pageNumber = 1,
        type = Renderable.Type.PassportQrCode,
        spec = BarcodeRenderSpec(
          width = 120,
          height = 120,
          matrixScale = 2F,
          positionX = 10F,
          positionY = 10F,
          colorId = BLUE_CMYK
        )
      )
    )
  }
}
