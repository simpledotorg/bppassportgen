package org.simple.clinic.bppassportgen.approvals.verifypassports

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGeneratingPassports : VerifyTestBase("uuids_passports.txt") {

  override val templateFilePath: String
    get() = resourceFilePath("bppassport-template.pdf")

  @Test
  fun `verify generating bp passports`() {
    app.run(
        uuidsToGenerate = uuids,
        outDirectory = outputDirectory,
        pageCount = 2,
        rowCount = 2,
        columnCount = 2,
        isSticker = false
    )

    runApprovals(8) { pdfNumber, pageNumber: Int -> "bp passport $pdfNumber.$pageNumber" }
  }
}
