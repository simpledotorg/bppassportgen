package org.simple.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.simple.bppassportgen.qrcodegen.QrCodeGenerator
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
    private val qrCodeGenerator: QrCodeGenerator
) : Callable<Output> {

  override fun call(): Output {
    return generatePages()
  }

  private fun generatePages(): Output {
    val sourceDocument = PDDocument.load(pdfBytes)

    check(templatePageIndexToRenderCode < sourceDocument.numberOfPages) { "PDF has only ${sourceDocument.numberOfPages} but asked to render code on $templatePageIndexToRenderCode" }

    val newDocument = PDDocument()
    val font = PDType0Font.load(newDocument, ByteArrayInputStream(fontBytes))

    uuidsGroupedByPage
        .forEach { uuidsInOnePage ->

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
              .map { sourcePage -> uuidsInOnePage.map { RenderContent(it, PdfUtil.clone(sourcePage)) } }

          pagesForCurrentBatch[templatePageIndexToRenderCode]
              .forEach { page -> renderQrCode(qrCodeGenerator, page.uuid, newDocument, page.pdPage, barcodeRenderSpec) }

          pagesForCurrentBatch[templatePageIndexToRenderShortCode]
              .forEach { page -> renderShortCode(page.uuid, newDocument, page.pdPage, font) }

          pagesForCurrentBatch
              .map { renderContents -> renderContents.map { it.pdPage } }
              .forEach { PdfUtil.mergePagesIntoOne(newDocument, it, rowCount, columnCount) }
        }

    return Output(source = sourceDocument, final = newDocument)
  }

  private fun renderQrCode(
      qrCodeGenerator: QrCodeGenerator,
      uuid: UUID,
      document: PDDocument,
      page: PDPage,
      spec: BarcodeRenderSpec
  ) {
    val bitMatrix = qrCodeGenerator.generateQrCode(uuid.toString(), spec.width, spec.height)
    val bitMatrixRenderable = BitMatrixRenderable(bitMatrix, matrixScale = spec.matrixScale)

    PdfUtil.streamForPage(document, page).use { contentStream ->

      bitMatrixRenderable.render(
          contentStream,
          spec.positionX,
          spec.positionY,
          applyForegroundColor = { it.setStrokingColor(spec.color) }
      )
    }
  }

  private fun renderShortCode(
      uuid: UUID,
      document: PDDocument,
      page: PDPage,
      font: PDType0Font
  ) {
    val shortCode = shortCodeForUuid(uuid)
    PdfUtil.streamForPage(document, page).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(shortcodeRenderSpec.color)
      contentStream.newLineAtOffset(shortcodeRenderSpec.positionX, shortcodeRenderSpec.positionY)
      contentStream.setCharacterSpacing(shortcodeRenderSpec.characterSpacing)
      contentStream.setFont(font, shortcodeRenderSpec.fontSize)
      contentStream.showText(shortCode)
      contentStream.endText()
    }
  }

  private fun shortCodeForUuid(uuid: UUID): String {
    return uuid
        .toString()
        .filter { it.isDigit() }
        .take(7)
        .let { shortCode ->
          val prefix = shortCode.substring(0, 3)
          val suffix = shortCode.substring(3)

          "$prefix $suffix"
        }
  }

  private data class RenderContent(val uuid: UUID, val pdPage: PDPage)
}
