package org.simple.bppassportgen.config.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
sealed class Renderable {

  enum class Type {
    QrCode,
    ShortCode
  }
}

@JsonTypeName("QrCode")
data class QrCodeRenderable(
    @JsonProperty("pageNumber")
    val pageNumber: Int,

    @JsonProperty("type")
    val type: Type,

    @JsonProperty("size")
    val size: Size,

    @JsonProperty("scale")
    val scale: Float = 1F,

    @JsonProperty("position")
    val position: Position,

    @JsonProperty("color")
    val colorId: String
) : Renderable()

@JsonTypeName("ShortCode")
data class ShortCodeRenderable(
    @JsonProperty("pageNumber")
    val pageNumber: Int,

    @JsonProperty("type")
    val type: Type,

    @JsonProperty("fontSize")
    val fontSize: Float,

    @JsonProperty("font")
    val fontId: String,

    @JsonProperty("position")
    val position: Position,

    @JsonProperty("color")
    val colorId: String,

    @JsonProperty("letterSpacing")
    val characterSpacing: Float = 1F
) : Renderable()
