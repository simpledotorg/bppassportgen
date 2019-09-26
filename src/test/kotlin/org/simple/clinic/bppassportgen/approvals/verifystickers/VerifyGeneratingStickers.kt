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
        rowCount = 2,
        columnCount = 2,
        isSticker = true
    )

    runApprovals(8) { pdfNumber, pageNumber: Int -> "passport sticker $pdfNumber.$pageNumber" }
  }
}
