package org.simple.clinic.bppassportgen

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.*
import strikt.assertions.*

@RunWith(JUnitParamsRunner::class)
class CanaryUnitTest {

  @Test
  fun `testing harness should work as expected`() {
    expectThat(2 + 2).isEqualTo(4)
  }

  @Test
  @Parameters(value = [
    "4|4",
    "-1|-1"
  ])
  fun `parameterized tests should work as expected`(
      actual: Int,
      expected: Int
  ) {
    expectThat(actual).isEqualTo(expected)
  }
}
