package org.simple.clinic.bppassportgen.approvals

import com.google.common.util.concurrent.MoreExecutors
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.approvaltests.Approvals
import org.approvaltests.namer.NamerFactory
import org.junit.Before
import org.simple.bppassportgen.PassportsGenerator
import org.simple.bppassportgen.RenderableSpec
import org.simple.clinic.bppassportgen.SavePdfToImage
import org.simple.clinic.bppassportgen.renderable.TestRenderSpecProvider
import org.simple.clinic.bppassportgen.util.ImageApprover
import org.simple.clinic.bppassportgen.util.NoOpConsolePrinter
import org.simple.clinic.bppassportgen.util.NoOpProgressPoll
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths

abstract class VerifyTestBase(
  private val renderSpecs: List<RenderableSpec>,
  private val fonts: Map<String, String>,
  private val colors: Map<String, PDColor>
) {
  private val bpPassportGenerationDirectoryName = "org.simple.bppassportgen.approvals_gen"
  internal val outputDirectory: File =
    Paths.get(System.getProperty("java.io.tmpdir"), bpPassportGenerationDirectoryName).toFile()

  protected val passportsGenerator: PassportsGenerator by lazy {
    PassportsGenerator(
      computationThreadPool = MoreExecutors.newDirectExecutorService(),
      ioThreadPool = MoreExecutors.newDirectExecutorService(),
      progressPoll = NoOpProgressPoll(),
      consolePrinter = NoOpConsolePrinter(),
      fonts = fonts,
      renderSpecProvider = TestRenderSpecProvider(),
      colorMap = colors
    )
  }

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
        Approvals.verify(ImageApprover.create(image, minimumSimilarity = 0.99))
      }
  }

  protected fun extractPagesFromSavedPdfsAsImages(): Map<Int, List<BufferedImage>> {
    return outputDirectory
      .listFiles { _, name -> name.endsWith(".pdf") }!!
      .map { file -> PDDocument.load(file) }
      .mapIndexed { index, document -> (index + 1) to document.use(SavePdfToImage::save) }
      .toMap()
  }
}
