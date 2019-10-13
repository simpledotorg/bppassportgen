package org.simple.clinic.bppassportgen.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test
import org.simple.bppassportgen.config.dto.Position
import org.simple.bppassportgen.config.dto.QrCodeRenderable
import org.simple.bppassportgen.config.dto.Renderable
import org.simple.bppassportgen.config.dto.ShortCodeRenderable
import org.simple.bppassportgen.config.dto.Size
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RenderableDtoTest {

  private val mapper = ObjectMapper().apply {
    registerModule(KotlinModule())
  }

  @Test
  fun `qr code renderable must be deserialized as expected`() {
    // given
    val json = """
      |{
      | "pageNumber": 1,
      | "type": "QrCode",
      | "size": {
      |   "w": 25,
      |   "h": 50
      | },
      | "scale": 1.5,
      | "position": {
      |   "x": 45.5,
      |   "y": 100.75
      | },
      | "color": "black"
      |}
    """.trimMargin()

    // when
    val renderable = mapper.readValue(json, Renderable::class.java)

    // then
    val expectedRenderable = QrCodeRenderable(
        pageNumber = 1,
        type = Renderable.Type.QrCode,
        size = Size(width = 25, height = 50),
        scale = 1.5F,
        position = Position(x = 45.5F, y = 100.75F),
        colorId = "black"
    )
    expectThat(renderable).isEqualTo(expectedRenderable)
  }

  @Test
  fun `short code renderable must be deserialized as expected`() {
    // given
    val json = """
      |{
      | "pageNumber": 1,
      | "type": "ShortCode",
      | "fontSize": 12.5,
      | "font": "Metropolis-Medium",
      | "position": {
      |   "x": 45.5,
      |   "y": 100.75
      | },
      | "color": "black",
      | "letterSpacing": "1.4"
      |}
    """.trimMargin()

    // when
    val renderable = mapper.readValue(json, Renderable::class.java)

    // then
    val expectedRenderable = ShortCodeRenderable(
        pageNumber = 1,
        type = Renderable.Type.ShortCode,
        fontSize = 12.5F,
        fontId = "Metropolis-Medium",
        position = Position(x = 45.5F, y = 100.75F),
        colorId = "black",
        characterSpacing = 1.4F
    )
    expectThat(renderable).isEqualTo(expectedRenderable)
  }
}
