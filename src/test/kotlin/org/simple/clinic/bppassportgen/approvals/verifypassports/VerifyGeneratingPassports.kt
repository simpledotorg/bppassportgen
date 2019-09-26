package org.simple.clinic.bppassportgen.approvals.verifypassports

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGeneratingPassports : VerifyTestBase("uuids_passports.txt") {

  @Test
  fun `verify generating bp passports`() {
    app.run(
        uuidsToGenerate = uuids,
        templateFilePath = resourceFilePath("bppassport-template.pdf"),
        outDirectory = outputDirectory,
        pageCount = 2,
        rowCount = 2,
        columnCount = 2,
        isSticker = false
    )

    runApprovals(8) { pageNumber: Int -> "bp passport $pageNumber" }
  }
}
