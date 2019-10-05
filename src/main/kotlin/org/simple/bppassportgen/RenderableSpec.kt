package org.simple.bppassportgen

import org.simple.bppassportgen.renderable.Renderable

data class RenderableSpec(val pageNumber: Int, val type: Renderable.Type, private val spec: Any) {

  @Suppress("UNCHECKED_CAST")
  fun <T> getSpecAs(): T {
    return spec as T
  }
}
