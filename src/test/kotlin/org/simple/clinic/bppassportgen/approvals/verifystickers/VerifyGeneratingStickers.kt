package org.simple.clinic.bppassportgen.approvals.verifystickers

import com.google.common.util.concurrent.MoreExecutors
import org.apache.pdfbox.pdmodel.PDDocument
import org.approvaltests.Approvals
import org.approvaltests.namer.NamerFactory
import org.junit.Before
import org.junit.Test
import org.simple.bppassportgen.App
import org.simple.clinic.bppassportgen.SavePdfToImage
import strikt.api.expectThat
import strikt.assertions.hasSize
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import java.util.UUID

class VerifyGeneratingStickers {

  private val outputDirectory: File = Paths.get(System.getProperty("java.io.tmpdir"), "bp_passports").toFile()
  private val uuids: List<UUID> = readUuids("uuids_stickers.txt")
  private val app = App(
      computationThreadPool = MoreExecutors.newDirectExecutorService(),
      ioThreadPool = MoreExecutors.newDirectExecutorService()
  )

  @Before
  fun setUp() {
    println(outputDirectory.absolutePath)
    outputDirectory.deleteRecursively()
  }

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

  private inline fun runApprovals(
      expectTotalNumberOfPages: Int,
      generateApprovalFileName: (Int) -> String
  ) {
    val generatedPdfPages = extractPagesFromSavedPdfsAsImages()

    expectThat(generatedPdfPages).hasSize(expectTotalNumberOfPages)
    generatedPdfPages
        .mapIndexed { index, image -> generateApprovalFileName(index + 1) to image }
        .forEach { (name, image) ->
          NamerFactory.additionalInformation = name
          Approvals.verify(image)
        }
  }

  private fun extractPagesFromSavedPdfsAsImages(): List<BufferedImage> {
    return outputDirectory
        .listFiles { _, name -> name.endsWith(".pdf") }!!
        .map { file -> PDDocument.load(file) }
        .flatMap { document -> document.use(SavePdfToImage::save) }
  }

  private fun readUuids(fileName: String): List<UUID> {
    return with(File(resourceFilePath(fileName))) {
      this.readLines().map { UUID.fromString(it) }
    }
  }

  private fun resourceFilePath(fileName: String) = javaClass.classLoader.getResource(fileName)!!.file
}
