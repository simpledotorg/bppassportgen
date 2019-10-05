package org.simple.bppassportgen

import org.simple.bppassportgen.renderable.Renderable

/**
 * This class maintains a list of [Renderable] objects for a given
 * page in the source document
 **/
data class PageSpec(private val map: Map<Int, List<Renderable>>) {

  fun renderablesForPageIndex(index: Int): List<Renderable> {
    return map.getOrDefault(index, emptyList())
  }
}
