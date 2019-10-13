package org.simple.bppassportgen.config.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Size(
    @JsonProperty("w")
    val width: Int,

    @JsonProperty("h")
    val height: Int
)
