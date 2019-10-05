package org.simple.clinic.bppassportgen

import com.google.zxing.common.BitMatrix
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.bppassportgen.renderable.qrcode.RenderBitMatrixOnPdContentStream
import org.simple.bppassportgen.renderable.qrcode.RenderBitMatrixOnPdContentStream.Line
import org.simple.bppassportgen.renderable.qrcode.RenderBitMatrixOnPdContentStream.Line.State.OFF
import org.simple.bppassportgen.renderable.qrcode.RenderBitMatrixOnPdContentStream.Line.State.ON
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(JUnitParamsRunner::class)
class BitMatrixRenderableTest {

  @Test
  @Parameters(method = "params for generating lines from BitMatrix")
  fun `lines should be generated from the BitMatrix as expected`(
      image: Array<BooleanArray>,
      expected: List<Line>
  ) {
    val bitMatrix = BitMatrix.parse(image)

    val renderable = RenderBitMatrixOnPdContentStream(bitMatrix, 1.0F)

    expectThat(renderable.lines).isEqualTo(expected)
  }

  @Suppress("BooleanLiteralArgument")
  fun `params for generating lines from BitMatrix`(): List<List<Any>> {
    fun testCase(
        image: Array<BooleanArray>,
        expected: List<Line>
    ): List<Any> {
      return listOf(image, expected)
    }

    return listOf(
        testCase(
            image = arrayOf(booleanArrayOf(false)),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 0F, yEnd = 0F, state = OFF)
            )
        ),
        testCase(
            image = arrayOf(booleanArrayOf(true)),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 0F, yEnd = 0F, state = ON)
            )
        ),
        testCase(
            image = arrayOf(booleanArrayOf(false, false, false, false)),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 3F, yEnd = 0F, state = OFF)
            )
        ),
        testCase(
            image = arrayOf(booleanArrayOf(true, true, true, true)),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 3F, yEnd = 0F, state = ON)
            )
        ),
        testCase(
            image = arrayOf(
                booleanArrayOf(true, true, false, false)
            ),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 1F, yEnd = 0F, state = ON),
                Line(xStart = 2F, yStart = 0F, xEnd = 3F, yEnd = 0F, state = OFF)
            )
        ),
        testCase(
            image = arrayOf(
                booleanArrayOf(false, false, true, true)
            ),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 1F, yEnd = 0F, state = OFF),
                Line(xStart = 2F, yStart = 0F, xEnd = 3F, yEnd = 0F, state = ON)
            )
        ),
        testCase(
            image = arrayOf(
                booleanArrayOf(true, false, true, false)
            ),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 0F, yEnd = 0F, state = ON),
                Line(xStart = 1F, yStart = 0F, xEnd = 1F, yEnd = 0F, state = OFF),
                Line(xStart = 2F, yStart = 0F, xEnd = 2F, yEnd = 0F, state = ON),
                Line(xStart = 3F, yStart = 0F, xEnd = 3F, yEnd = 0F, state = OFF)
            )
        ),
        testCase(
            image = arrayOf(
                booleanArrayOf(false, true, false, true)
            ),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 0F, yEnd = 0F, state = OFF),
                Line(xStart = 1F, yStart = 0F, xEnd = 1F, yEnd = 0F, state = ON),
                Line(xStart = 2F, yStart = 0F, xEnd = 2F, yEnd = 0F, state = OFF),
                Line(xStart = 3F, yStart = 0F, xEnd = 3F, yEnd = 0F, state = ON)
            )
        ),
        testCase(
            image = arrayOf(
                booleanArrayOf(true, true, true, false, false, true, true, false)
            ),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 2F, yEnd = 0F, state = ON),
                Line(xStart = 3F, yStart = 0F, xEnd = 4F, yEnd = 0F, state = OFF),
                Line(xStart = 5F, yStart = 0F, xEnd = 6F, yEnd = 0F, state = ON),
                Line(xStart = 7F, yStart = 0F, xEnd = 7F, yEnd = 0F, state = OFF)
            )
        ),
        testCase(
            image = arrayOf(
                booleanArrayOf(true, true, true, true, true, true, true, true),
                booleanArrayOf(false, false, false, false, false, false, false, false),
                booleanArrayOf(true, true, true, true, false, false, false, false),
                booleanArrayOf(false, false, false, false, true, true, true, true),
                booleanArrayOf(true, true, true, false, false, true, true, false)
            ),
            expected = listOf(
                Line(xStart = 0F, yStart = 0F, xEnd = 7F, yEnd = 0F, state = ON),

                Line(xStart = 0F, yStart = 1F, xEnd = 7F, yEnd = 1F, state = OFF),

                Line(xStart = 0F, yStart = 2F, xEnd = 3F, yEnd = 2F, state = ON),
                Line(xStart = 4F, yStart = 2F, xEnd = 7F, yEnd = 2F, state = OFF),

                Line(xStart = 0F, yStart = 3F, xEnd = 3F, yEnd = 3F, state = OFF),
                Line(xStart = 4F, yStart = 3F, xEnd = 7F, yEnd = 3F, state = ON),

                Line(xStart = 0F, yStart = 4F, xEnd = 2F, yEnd = 4F, state = ON),
                Line(xStart = 3F, yStart = 4F, xEnd = 4F, yEnd = 4F, state = OFF),
                Line(xStart = 5F, yStart = 4F, xEnd = 6F, yEnd = 4F, state = ON),
                Line(xStart = 7F, yStart = 4F, xEnd = 7F, yEnd = 4F, state = OFF)
            )
        )
    )
  }
}
