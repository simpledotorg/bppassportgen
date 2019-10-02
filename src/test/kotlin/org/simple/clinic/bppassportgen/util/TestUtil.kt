package org.simple.clinic.bppassportgen.util

import java.io.File
import java.util.UUID

fun readUuids(fileName: String): List<UUID> {
  return with(File(resourceFilePath(fileName))) {
    this.readLines().map { UUID.fromString(it) }
  }
}

fun resourceFilePath(fileName: String): String {
  return ClassLoader.getSystemClassLoader().getResource(fileName)!!.file
}
