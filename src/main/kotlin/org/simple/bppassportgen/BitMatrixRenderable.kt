package org.simple.bppassportgen

import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix

class BitMatrixRenderable(bitMatrix: BitMatrix) {
  val lines: List<Line>

  init {
    lines = generateLines(bitMatrix)
  }

  private fun generateLines(bitMatrix: BitMatrix): List<Line> {

    return (0 until bitMatrix.height)
        .map { rowIndex ->
          rowIndex to bitMatrix.getRow(rowIndex, null)
        }
        .flatMap { (rowIndex, bitArray) -> bitArrayToLines(bitArray, rowIndex) }
        .toList()
  }

  private fun bitArrayToLines(bitArray: BitArray, rowIndex: Int): List<Line> {

    return (0 until bitArray.size)
        .fold(LineAccumulator(0, bitArray.state(0))) { accumulator, columnIndex ->
          val state = bitArray.state(columnIndex)

          if (state != accumulator.state) {
            // Add a new line, reset the line start in the accumulator
            val line = Line(
                xStart = accumulator.lineStart.toFloat(),
                yStart = rowIndex.toFloat(),
                xEnd = (columnIndex - 1).coerceAtLeast(0).toFloat(),
                yEnd = rowIndex.toFloat(),
                state = accumulator.state
            )

            accumulator.lines += line
            accumulator.lineStart = columnIndex
            accumulator.state = state
          }

          // Handle the last line
          val shouldCreateLastLine = (columnIndex == bitArray.size - 1) &&
              (accumulator.lines.isEmpty() || accumulator.lines.last().xEnd != columnIndex.toFloat())

          if (shouldCreateLastLine) {
            val line = Line(
                xStart = accumulator.lineStart.toFloat(),
                yStart = rowIndex.toFloat(),
                xEnd = columnIndex.toFloat(),
                yEnd = rowIndex.toFloat(),
                state = state
            )

            accumulator.lines += line
          }

          accumulator
        }.lines
  }

  private data class LineAccumulator(
      var lineStart: Int,
      var state: Line.State,
      val lines: MutableList<Line> = mutableListOf()
  )

  private fun BitArray.state(index: Int): Line.State {
    return when(get(index)) {
      true -> Line.State.ON
      false -> Line.State.OFF
    }
  }

  data class Line(
      val xStart: Float,
      val yStart: Float,
      val xEnd: Float,
      val yEnd: Float,
      val state: State
  ) {

    enum class State {
      ON, OFF
    }
  }
}
