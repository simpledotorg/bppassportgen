package org.simple.clinic.bppassportgen.approvals.verifystickers

import org.junit.Test
import org.simple.clinic.bppassportgen.approvals.VerifyTestBase
import org.simple.clinic.bppassportgen.util.resourceFilePath

class VerifyGeneratingStickers : VerifyTestBase("uuids_stickers.txt") {

  override val templateFilePath: String = resourceFilePath("passportsticker-template.pdf")

  override val pageCount: Int = 2

  override val rowCount: Int = 2

  override val columnCount: Int = 2

  override val isSticker: Boolean = true

  @Test
  fun `verify generating bp stickers`() {
    app.run(uuidsToGenerate = uuids)

    runApprovals(8) { pdfNumber, pageNumber: Int -> "passport sticker $pdfNumber.$pageNumber" }
  }
}
