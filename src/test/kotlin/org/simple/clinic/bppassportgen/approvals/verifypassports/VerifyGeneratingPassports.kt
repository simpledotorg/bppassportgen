package org.simple.clinic.bppassportgen.approvals.verifypassports

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGeneratingPassports : VerifyTestBase("uuids_passports.txt") {

  override val templateFilePath: String = resourceFilePath("bppassport-template.pdf")

  override val pageCount: Int = 2

  override val rowCount: Int = 2

  @Test
  fun `verify generating bp passports`() {
    app.run(
        uuidsToGenerate = uuids,
        columnCount = 2,
        isSticker = false
    )

    runApprovals(8) { pdfNumber, pageNumber: Int -> "bp passport $pdfNumber.$pageNumber" }
  }
}
