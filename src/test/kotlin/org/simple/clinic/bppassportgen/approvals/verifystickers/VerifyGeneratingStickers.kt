package org.simple.clinic.bppassportgen.approvals.verifystickers

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase
import org.simple.clinic.bppassportgen.util.readUuids
import org.simple.clinic.bppassportgen.util.resourceFilePath

class VerifyGeneratingStickers : VerifyTestBase(
    templateFilePath = resourceFilePath("passportsticker-template.pdf"),
    pageCount = 2,
    rowCount = 2,
    columnCount = 2,
    isSticker = true
) {

  @Test
  fun `verify generating bp stickers`() {
    app.run(uuidsToGenerate = readUuids("uuids_stickers.txt"))

    runApprovals(8) { pdfNumber, pageNumber: Int -> "passport sticker $pdfNumber.$pageNumber" }
  }
}
