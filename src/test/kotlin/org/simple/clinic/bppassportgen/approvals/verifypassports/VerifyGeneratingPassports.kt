package org.simple.clinic.bppassportgen.approvals.verifypassports

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase
import org.simple.clinic.bppassportgen.util.readUuids
import org.simple.clinic.bppassportgen.util.resourceFilePath

class VerifyGeneratingPassports : VerifyTestBase(
    templateFilePath = resourceFilePath("bppassport-template.pdf"),
    pageCount = 2,
    rowCount = 2,
    columnCount = 2,
    isSticker = false
) {

  @Test
  fun `verify generating bp passports`() {
    app.run(uuidsToGenerate = readUuids("uuids_passports.txt"))

    runApprovals(8) { pdfNumber, pageNumber: Int -> "bp passport $pdfNumber.$pageNumber" }
  }
}
