package org.simple.clinic.bppassportgen.approvals.verifyv2

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGenerationOfPdf : VerifyTestBase(
    templateFilePath = "blank.pdf",
    pageCount = 2,
    rowCount = 2,
    columnCount = 2,
    isSticker = false
) {

  @Test
  fun `verify generation of PDFs`() {
    // given


    // when

    // then
  }
}
