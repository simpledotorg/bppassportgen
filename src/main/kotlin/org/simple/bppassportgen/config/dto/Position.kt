package org.simple.bppassportgen.config.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Position(
    @JsonProperty("x")
    val x: Float,

    @JsonProperty("y")
    val y: Float
)
