package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.simple.bppassportgen.renderable.Renderable
import java.util.concurrent.Callable

class GenerateBpPassportTask(
  private val pdfBytes: ByteArray,
  private val rowCount: Int,
  private val columnCount: Int,
  private val pageSpecs: List<List<PageSpec>>,
  private val documentFactory: PdDocumentFactory
) : Callable<Output> {

  override fun call(): Output {
    return generatePages()
  }

  private fun generatePages(): Output {
    val openedDocument = documentFactory.emptyDocument()
    val sourceDocument = PDDocument.load(pdfBytes)

    pageSpecs
      .forEach { specsGroupedByPage ->
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
            specsGroupedByPage
              .map { it.renderablesForPageIndex(sourcePageIndex) }
              .map { renderables ->
                RenderContent(
                  pdPage = PdfUtil.clone(sourcePage),
                  renderables = renderables
                )
              }
          }

        pagesForCurrentBatch
          .flatten()
          .forEach { renderContent ->
            renderContent
              .renderables
              .forEach { it.render(openedDocument, renderContent.pdPage) }
          }

        pagesForCurrentBatch
          .map { renderContents -> renderContents.map { it.pdPage } }
          .forEach { PdfUtil.mergePagesIntoOne(openedDocument.document, it, rowCount, columnCount) }
      }

    return Output(source = sourceDocument, final = openedDocument.document)
  }

  private data class RenderContent(
    val pdPage: PDPage,
    val renderables: List<Renderable>
  )
}

