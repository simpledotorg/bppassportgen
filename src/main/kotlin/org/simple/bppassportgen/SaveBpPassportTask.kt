package org.simple.bppassportgen

import java.io.File
import java.util.UUID

class SaveBpPassportTask(
    val output: Output,
    val uuid: UUID,
    val directory: File
) : Runnable {

  override fun run() {
    output.use { it.final.save(File(directory, "$uuid.pdf")) }
  }
}
