package org.simple.clinic.bppassportgen.verifystickers

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
import strikt.api.*
import strikt.assertions.*

class VerifyGeneratingStickers {

  private val outputDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "bp_passports").toFile()

  @Before
  fun setUp() {
    println(outputDirectory.absolutePath)
    outputDirectory.deleteRecursively()
  }

  @Test
  fun `verify generating bp stickers`() {
    val uuids = readUuids("uuids_stickers.txt")
    App(
        computationThreadPool = MoreExecutors.newDirectExecutorService(),
        ioThreadPool = MoreExecutors.newDirectExecutorService()
    ).run(
        uuidsToGenerate = uuids,
        templateFilePath = resourceFilePath("passportsticker-template.pdf"),
        outDirectory = outputDirectory,
        pageCount = 2,
        rowCount = 4,
        columnCount = 4,
        isSticker = true
    )

    val generatedPdfPages = outputDirectory
        .listFiles { _, name -> name.endsWith(".pdf") }!!
        .map { file -> PDDocument.load(file) }
        .flatMap(SavePdfToImage::save)

    expectThat(generatedPdfPages).hasSize(2)

    generatedPdfPages
        .mapIndexed { index, image -> "passport sticker ${index + 1}" to image }
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
