package org.simple.clinic.bppassportgen.verifypassports

import com.google.common.util.concurrent.MoreExecutors
import org.apache.pdfbox.pdmodel.PDDocument
import org.approvaltests.Approvals
import org.approvaltests.namer.NamerFactory
import org.junit.Before
import org.junit.Test
import org.simple.bppassportgen.App
import org.simple.clinic.bppassportgen.SavePdfToImage
import java.io.File
import java.nio.file.Paths
import java.util.UUID

class VerifyGeneratingPassports {

  private val outputDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "bp_passports").toFile()

  @Before
  fun setUp() {
    println(outputDirectory.absolutePath)
    outputDirectory.deleteRecursively()
  }

  @Test
  fun `verify generating bp passports`() {
    val uuids = readUuids("uuids_passports.txt")
    App(
        computationThreadPool = MoreExecutors.newDirectExecutorService(),
        ioThreadPool = MoreExecutors.newDirectExecutorService()
    ).run(
        uuidsToGenerate = uuids,
        templateFilePath = resourceFilePath("bppassport-template.pdf"),
        outDirectory = outputDirectory,
        pageCount = 2,
        rowCount = 2,
        columnCount = 2,
        isSticker = false
    )

    val generatedPdfPages = outputDirectory
        .listFiles { _, name -> name.endsWith(".pdf") }!!
        .map { file -> PDDocument.load(file) }
        .flatMap(SavePdfToImage::save)

    generatedPdfPages
        .mapIndexed { index, image -> "bp passport ${index + 1}" to image }
        .forEach { (name, image) ->
          NamerFactory.additionalInformation = name
          Approvals.verify(image)
        }
  }

  private fun readUuids(fileName: String): List<UUID> {
    return with(File(resourceFilePath(fileName))) {
      this.readLines().map { UUID.fromString(it) }
    }
  }

  private fun resourceFilePath(fileName: String) = javaClass.classLoader.getResource(fileName)!!.file
}
