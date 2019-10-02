package org.simple.clinic.bppassportgen.approvals.verifystickers

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGeneratingStickers : VerifyTestBase("uuids_stickers.txt") {

  override val templateFilePath: String = resourceFilePath("passportsticker-template.pdf")

  @Test
  fun `verify generating bp stickers`() {
    app.run(
        uuidsToGenerate = uuids,
        pageCount = 2,
        rowCount = 2,
        columnCount = 2,
        isSticker = true
    )

    runApprovals(8) { pdfNumber, pageNumber: Int -> "passport sticker $pdfNumber.$pageNumber" }
  }
}
