package org.simple.bppassportgen.config.helpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB

class PDColorDeserializer : StdDeserializer<PDColor>(PDColor::class.java) {

  private val rgbFields = setOf("r", "g", "b")
  private val cmykFields = setOf("c", "m", "y", "k")

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PDColor {
    val node = p.codec.readTree<JsonNode>(p)

    val fieldNames = node.fieldNames().asSequence().toSet()

    return when {
      hasAllRgbFields(fieldNames) -> parseAsRgbColor(node)
      hasAllCmykFields(fieldNames) -> parseAsCmykColor(node)
      else -> {
        val message = "Expected fields [r, g, b] or [c, m, y, k], but one or more were missing!"

        throw MismatchedInputException.from(p, PDColor::class.java, message)
      }
    }
  }

  private fun hasAllRgbFields(fieldNames: Set<String>) = fieldNames.intersect(rgbFields) == rgbFields

  private fun parseAsRgbColor(node: JsonNode): PDColor {
    val r = node["r"].floatValue()
    val g = node["g"].floatValue()
    val b = node["b"].floatValue()

    return PDColor(floatArrayOf(r, g, b), COSName.DEVICERGB, PDDeviceRGB.INSTANCE)
  }

  private fun hasAllCmykFields(fieldNames: Set<String>) = fieldNames.intersect(cmykFields) == cmykFields

  private fun parseAsCmykColor(node: JsonNode): PDColor {
    val c = node["c"].floatValue()
    val m = node["m"].floatValue()
    val y = node["y"].floatValue()
    val k = node["k"].floatValue()

    return PDColor(floatArrayOf(c, m, y, k), COSName.DEVICECMYK, PDDeviceCMYK.INSTANCE)
  }
}
