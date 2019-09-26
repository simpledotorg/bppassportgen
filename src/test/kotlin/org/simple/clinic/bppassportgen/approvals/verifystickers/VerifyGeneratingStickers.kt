package org.simple.clinic.bppassportgen.approvals.verifystickers

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase

class VerifyGeneratingStickers : VerifyTestBase("uuids_stickers.txt") {

  @Test
  fun `verify generating bp stickers`() {
    app.run(
        uuidsToGenerate = uuids,
        templateFilePath = resourceFilePath("passportsticker-template.pdf"),
        outDirectory = outputDirectory,
        pageCount = 2,
        rowCount = 4,
        columnCount = 4,
        isSticker = true
    )

    runApprovals(2) { pageNumber: Int -> "passport sticker $pageNumber" }
  }
}
