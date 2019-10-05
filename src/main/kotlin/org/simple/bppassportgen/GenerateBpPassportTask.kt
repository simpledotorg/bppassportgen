package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.simple.bppassportgen.qrcodegen.QrCodeGenerator
import org.simple.bppassportgen.renderable.Renderable
import org.simple.bppassportgen.renderable.qrcode.BarcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderable
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.concurrent.Callable

class GenerateBpPassportTask(
    private val pdfBytes: ByteArray,
    private val fontBytes: ByteArray,
    private val uuidsGroupedByPage: List<List<UUID>>,
    private val rowCount: Int,
    private val columnCount: Int,
    private val barcodeRenderSpec: BarcodeRenderSpec,
    private val shortcodeRenderSpec: ShortcodeRenderSpec,
    private val templatePageIndexToRenderCode: Int,
    private val templatePageIndexToRenderShortCode: Int,
    private val qrCodeGenerator: QrCodeGenerator,
    private val pageSpecs: List<List<PageSpec>>,
    private val newDocument: PDDocument
) : Callable<Output> {

  override fun call(): Output {
    return generatePages()
  }

  private fun generatePages(): Output {
    val sourceDocument = PDDocument.load(pdfBytes)

    check(templatePageIndexToRenderCode < sourceDocument.numberOfPages) { "PDF has only ${sourceDocument.numberOfPages} but asked to render code on $templatePageIndexToRenderCode" }

    val font = PDType0Font.load(newDocument, ByteArrayInputStream(fontBytes))

    uuidsGroupedByPage
        .forEachIndexed { pageIndex, uuidsInOnePage ->

          // Only required until generation of renderables is moved
          val pageSpecsForThisBatch = pageSpecs.getOrElse(pageIndex) { emptyList() }

          /*
          * This maintains a clone of each page in the template document
          * for every UUID that is supposed to go into a single page in
          * the final document.
          *
          * For example, if the template document has three pages, and
          * we are supposed to render 16 UUIDS in a single page in the
          * final merged document, this will end up looking something
          * like:
          *
          * [0] -> [page 0, uuid 0] -> [page 0, uuid 1] -> ... [page 0, uuid 15]
          *  |
          * [1] -> [page 1, uuid 0] -> [page 1, uuid 1] -> ... [page 1, uuid 15]
          *  |
          * [2] -> [page 2, uuid 0] -> [page 2, uuid 1] -> ... [page 2, uuid 15]
          *
          * TODO (vs 02-10-2019): Find a better abstraction for this
          **/
          val pagesForCurrentBatch = sourceDocument
              .pages
              .mapIndexed { sourcePageIndex, sourcePage ->
                uuidsInOnePage.mapIndexed { uuidIndex, uuid ->

                  val renderablesForPageIndex = pageSpecsForThisBatch
                      // Only required until generation of renderables is moved
                      .getOrElse(uuidIndex) { PageSpec(emptyMap()) }
                      .renderablesForPageIndex(sourcePageIndex)

                  RenderContent(
                      pdPage = PdfUtil.clone(sourcePage),
                      renderables = generateRenderables(sourcePageIndex, uuid, font) + renderablesForPageIndex
                  )
                }
              }

          pagesForCurrentBatch
              .flatten()
              .forEach { renderContent ->
                renderContent
                    .renderables
                    .forEach { it.render(newDocument, renderContent.pdPage) }
              }

          pagesForCurrentBatch
              .map { renderContents -> renderContents.map { it.pdPage } }
              .forEach { PdfUtil.mergePagesIntoOne(newDocument, it, rowCount, columnCount) }
        }

    return Output(source = sourceDocument, final = newDocument)
  }

  private fun generateRenderables(
      sourcePageIndex: Int,
      uuid: UUID,
      font: PDType0Font
  ): List<Renderable> {
    return when (sourcePageIndex) {
      templatePageIndexToRenderShortCode -> listOf(ShortcodeRenderable(uuid, font, shortcodeRenderSpec))
      else -> emptyList()
    }
  }

  private data class RenderContent(
      val pdPage: PDPage,
      val renderables: List<Renderable>
  )
}

