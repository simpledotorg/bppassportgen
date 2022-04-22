package org.simple.bppassportgen.generator

enum class PageSize(val rows: Int, val columns: Int) {
  A0(rows = 4, columns = 8),
  A1(rows = 4, columns = 4),
  A2(rows = 2, columns = 4),
  A3(rows = 2, columns = 2),
  A4(rows = 1, columns = 2)
}
