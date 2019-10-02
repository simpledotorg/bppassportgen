package org.simple.clinic.bppassportgen.approvals.verifypassports

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase
import org.simple.clinic.bppassportgen.util.readUuids
import org.simple.clinic.bppassportgen.util.resourceFilePath

class VerifyGeneratingPassports : VerifyTestBase() {

  override val templateFilePath: String = resourceFilePath("bppassport-template.pdf")

  override val pageCount: Int = 2

  override val rowCount: Int = 2

  override val columnCount: Int = 2

  override val isSticker: Boolean = false

  @Test
  fun `verify generating bp passports`() {
    app.run(uuidsToGenerate = readUuids("uuids_passports.txt"))

    runApprovals(8) { pdfNumber, pageNumber: Int -> "bp passport $pdfNumber.$pageNumber" }
  }
}
