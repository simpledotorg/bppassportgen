package org.simple.clinic.bppassportgen.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.module.SimpleModule
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.bppassportgen.config.helpers.PDColorDeserializer
import strikt.api.expect
import strikt.assertions.isEqualTo

@RunWith(JUnitParamsRunner::class)
class PDColorDeserializerTest {

  private val mapper = ObjectMapper().apply {
    val module = SimpleModule()
    module.addDeserializer(PDColor::class.java, PDColorDeserializer())
    registerModule(module)
  }

  @Test
  fun `rgb colors must deserialize as expected`() {
    // given
    val json = """
      |{
      | "r": 0.25,
      | "g": 0.5,
      | "b": 0.75
      |}
    """.trimMargin()

    // when
    val color = mapper.readValue(json, PDColor::class.java)

    // then
    expect {
      that(color.components).isEqualTo(floatArrayOf(0.25F, 0.5F, 0.75F))
      that(color.colorSpace).isEqualTo(PDDeviceRGB.INSTANCE)
      that(color.patternName).isEqualTo(COSName.DEVICERGB)
    }
  }

  @Test
  fun `cmyk colors must deserialize as expected`() {
    // given
    val json = """
      |{
      | "c": 0.0,
      | "m": 0.31,
      | "y": 0.03,
      | "k": 0.54
      |}
    """.trimMargin()

    // when
    val color = mapper.readValue(json, PDColor::class.java)

    // then
    expect {
      that(color.components).isEqualTo(floatArrayOf(0F, 0.31F, 0.03F, 0.54F))
      that(color.colorSpace).isEqualTo(PDDeviceCMYK.INSTANCE)
      that(color.patternName).isEqualTo(COSName.DEVICECMYK)
    }
  }

  @Test(expected = MismatchedInputException::class)
  @Parameters(method = "params for missing rgb values")
  fun `missing properties must throw an appropriate exception when parsing rgb colors`(json: String) {
    mapper.readValue(json, PDColor::class.java)
  }

  @Suppress("Unused")
  private fun `params for missing rgb values`(): List<String> {
    return listOf(
        """
          |{
          | "r": 0.0,
          | "g": 0.0
          |}
        """,
        """
          |{
          | "r": 0.0,
          | "b": 0.0
          |}
        """,
        """
          |{
          | "g": 0.0,
          | "b": 0.0
          |}
        """,
        """
          |{
          | "b": 0.0
          |}
        """,
        """
          |{
          | "g": 0.0
          |}
        """,
        """
          |{
          | "r": 0.0
          |}
        """,
        """
          |{
          |}
        """
    ).map { it.trimMargin() }
  }

  @Test(expected = MismatchedInputException::class)
  @Parameters(method = "params for missing cmyk values")
  fun `missing properties must throw an appropriate exception when parsing cmyk colors`(json: String) {
    mapper.readValue(json, PDColor::class.java)
  }

  @Suppress("Unused")
  private fun `params for missing cmyk values`(): List<String> {
    return listOf(
        """
          |{
          | "c": 0.0,
          | "m": 0.0,
          | "y": 0.0
          |}
        """,
        """
          |{
          | "c": 0.0,
          | "m": 0.0,
          | "k": 0.0
          |}
        """,
        """
          |{
          | "c": 0.0,
          | "y": 0.0,
          | "k": 0.0
          |}
        """,
        """
          |{
          | "m": 0.0,
          | "y": 0.0,
          | "k": 0.0
          |}
        """,
        """
          |{
          | "c": 0.0,
          | "m": 0.0
          |}
        """,
        """
          |{
          | "c": 0.0,
          | "y": 0.0
          |}
        """,
        """
          |{
          | "c": 0.0,
          | "k": 0.0
          |}
        """,
        """
          |{
          | "y": 0.0,
          | "m": 0.0
          |}
        """,
        """
          |{
          | "y": 0.0,
          | "k": 0.0
          |}
        """,
        """
          |{
          | "m": 0.0,
          | "k": 0.0
          |}
        """,
        """
          |{
          | "c": 0.0
          |}
        """,
        """
          |{
          | "m": 0.0
          |}
        """,
        """
          |{
          | "y": 0.0
          |}
        """,
        """
          |{
          | "k": 0.0
          |}
        """,
        """
          |{
          |}
        """
    ).map { it.trimMargin() }
  }
}
