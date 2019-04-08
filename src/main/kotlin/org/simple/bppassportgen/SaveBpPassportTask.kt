package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.util.UUID

class SaveBpPassportTask(
    val document: PDDocument,
    val uuid: UUID,
    val directory: File
): Runnable {

  override fun run() {
    val outputFile = File(directory, "$uuid.pdf")
    document.use { it.save(outputFile) }
  }
}
