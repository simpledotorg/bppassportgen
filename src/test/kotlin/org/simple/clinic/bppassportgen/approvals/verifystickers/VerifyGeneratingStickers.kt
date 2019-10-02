package org.simple.clinic.bppassportgen.approvals.verifystickers

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGeneratingStickers : VerifyTestBase("uuids_stickers.txt") {

  override val templateFilePath: String = resourceFilePath("passportsticker-template.pdf")

  override val pageCount: Int = 2

  @Test
  fun `verify generating bp stickers`() {
    app.run(
        uuidsToGenerate = uuids,
        rowCount = 2,
        columnCount = 2,
        isSticker = true
    )

    runApprovals(8) { pdfNumber, pageNumber: Int -> "passport sticker $pdfNumber.$pageNumber" }
  }
}
