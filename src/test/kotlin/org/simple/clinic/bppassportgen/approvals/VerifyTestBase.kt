package org.simple.clinic.bppassportgen.approvals

import com.google.common.util.concurrent.MoreExecutors
import org.apache.pdfbox.pdmodel.PDDocument
import org.approvaltests.Approvals
import org.approvaltests.namer.NamerFactory
import org.junit.Before
import org.simple.bppassportgen.App
import org.simple.clinic.bppassportgen.SavePdfToImage
import org.simple.clinic.bppassportgen.util.NoOpConsolePrinter
import org.simple.clinic.bppassportgen.util.NoOpProgressPoll
import strikt.api.expectThat
import strikt.assertions.isEqualTo
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
      ioThreadPool = MoreExecutors.newDirectExecutorService(),
      progressPoll = NoOpProgressPoll(),
      consolePrinter = NoOpConsolePrinter()
  )

  @Before
  fun setUp() {
    outputDirectory.deleteRecursively()
  }

  protected inline fun runApprovals(
      @Suppress("SameParameterValue") expectTotalNumberOfPages: Int,
      generateApprovalFileName: (Int, Int) -> String
  ) {
    val generatedPdfPages = extractPagesFromSavedPdfsAsImages()
    val pageCount = generatedPdfPages
        .values
        .sumBy { it.size }

    expectThat(pageCount).isEqualTo(expectTotalNumberOfPages)
    generatedPdfPages
        .flatMap { (pdfNumber, images) ->
          images.mapIndexed { pageIndex, pageImage -> generateApprovalFileName(pdfNumber, pageIndex + 1) to pageImage }
        }
        .forEach { (name, image) ->
          NamerFactory.additionalInformation = name
          Approvals.verify(image)
        }
  }

  protected fun extractPagesFromSavedPdfsAsImages(): Map<Int, List<BufferedImage>> {
    return outputDirectory
        .listFiles { _, name -> name.endsWith(".pdf") }!!
        .map { file -> PDDocument.load(file) }
        .mapIndexed { index, document -> (index + 1) to document.use(SavePdfToImage::save) }
        .toMap()
  }

  private fun readUuids(fileName: String): List<UUID> {
    return with(File(resourceFilePath(fileName))) {
      this.readLines().map { UUID.fromString(it) }
    }
  }

  protected fun resourceFilePath(fileName: String): String = javaClass.classLoader.getResource(fileName)!!.file
}
