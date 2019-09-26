package org.simple.clinic.bppassportgen.approvals

import com.google.common.util.concurrent.MoreExecutors
import org.apache.pdfbox.pdmodel.PDDocument
import org.approvaltests.Approvals
import org.approvaltests.namer.NamerFactory
import org.junit.Before
import org.simple.bppassportgen.App
import org.simple.clinic.bppassportgen.SavePdfToImage
import strikt.api.expectThat
import strikt.assertions.hasSize
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import java.util.UUID

open class VerifyTestBase(uuidFileResourcePath: String) {
  private val bpPassportGenerationDirectoryName = "org.simple.bppassportgen.approvals_gen"
  protected val outputDirectory: File = Paths.get(System.getProperty("java.io.tmpdir"), bpPassportGenerationDirectoryName).toFile()
  protected val uuids: List<UUID> = readUuids(uuidFileResourcePath)
  protected val app = App(
      computationThreadPool = MoreExecutors.newDirectExecutorService(),
      ioThreadPool = MoreExecutors.newDirectExecutorService()
  )

  @Before
  fun setUp() {
    outputDirectory.deleteRecursively()
  }

  protected inline fun runApprovals(
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

  protected fun extractPagesFromSavedPdfsAsImages(): List<BufferedImage> {
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

  protected fun resourceFilePath(fileName: String): String = javaClass.classLoader.getResource(fileName)!!.file
}
