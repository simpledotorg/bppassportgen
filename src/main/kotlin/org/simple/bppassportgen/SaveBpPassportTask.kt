package org.simple.bppassportgen

import java.io.File
import java.util.Locale

class SaveBpPassportTask(
  val output: Output,
  val taskNumber: Int,
  val totalSize: Int,
  val directory: File
) : Runnable {

  override fun run() {
    val format = "%0${numberOfDigits(totalSize)}d.pdf"
    output.use { it.final.save(File(directory, String.format(Locale.ENGLISH, format, taskNumber))) }
  }

  private fun numberOfDigits(number: Int): Int {
    var count = 0
    var next = number

    while (next > 0) {
      next /= 10
      count++
    }

    return count
  }
}
