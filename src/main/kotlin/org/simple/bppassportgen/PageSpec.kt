package org.simple.bppassportgen

import org.simple.bppassportgen.renderable.Renderable

/**
 * This class maintains a list of [Renderable] objects for a given
 * page in the source document
 *
 * Comment by author: I don't know what a good name for this is, but
 * it signifies is this - "For every clone of the template document
 * that is made, draw these renderables on each clone".
 **/
data class PageSpec(private val map: Map<Int, List<Renderable>>) {

  fun renderablesForPageIndex(index: Int): List<Renderable> {
    return map.getOrDefault(index, emptyList())
  }
}
