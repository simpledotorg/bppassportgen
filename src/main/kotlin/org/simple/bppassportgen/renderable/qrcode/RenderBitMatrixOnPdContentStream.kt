package org.simple.bppassportgen.renderable.qrcode

import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.util.Matrix
import java.awt.Color

/**
 * The reason this class exists is because we need to render the QR code to the PDF in the CMYK color space.
 *
 * The default image generation mechanism does not support rendering as CMYK, so we read the BitMatrix from Zxing and
 * manually draw lines as required to render the QR code on the PDF.
 **/
class RenderBitMatrixOnPdContentStream(bitMatrix: BitMatrix, private val matrixScale: Float = 1F) {
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

  fun render(
    contentStream: PDPageContentStream,
    x: Float,
    y: Float,
    drawBackground: Boolean = false,
    applyForegroundColor: (PDPageContentStream) -> Unit = { it.setStrokingColor(Color.BLACK) },
    applyBackgroundColor: (PDPageContentStream) -> Unit = { it.setStrokingColor(Color.WHITE) }
  ) {
    val matrix = Matrix()
    matrix.scale(matrixScale, matrixScale)

    contentStream.saveGraphicsState()
    contentStream.transform(matrix)
    val (foregroundLines, backgroundLines) = lines.partition { it.state == Line.State.ON }

    contentStream.setLineCapStyle(2)
    contentStream.setLineWidth(1.1F)

    applyForegroundColor(contentStream)
    foregroundLines.forEach { line ->
      drawLine(line, x, y, contentStream)
    }

    if (drawBackground) {
      applyBackgroundColor(contentStream)
      backgroundLines.forEach { line ->
        drawLine(line, x, y, contentStream)
      }
    }
    contentStream.restoreGraphicsState()
  }

  private fun drawLine(
    line: Line,
    x: Float,
    y: Float,
    contentStream: PDPageContentStream
  ) {
    val lineStartX = line.xStart + x
    val lineStartY = line.yStart + y
    val lineEndX = line.xEnd + x
    val lineEndY = line.yEnd + y

    contentStream.moveTo(lineStartX, lineStartY)
    contentStream.lineTo(lineEndX, lineEndY)
    contentStream.stroke()
  }

  private data class LineAccumulator(
    var lineStart: Int,
    var state: Line.State,
    val lines: MutableList<Line> = mutableListOf()
  )

  private fun BitArray.state(index: Int): Line.State {
    return when (get(index)) {
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
