package org.simple.bppassportgen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.concurrent.Callable

class GenerateBpPassportTask(
    private val pdfBytes: ByteArray,
    private val fontBytes: ByteArray,
    private val uuidsGroupedByPage: List<List<UUID>>,
    private val qrCodeWriter: QRCodeWriter,
    private val hints: Map<EncodeHintType, Any>,
    private val shortCodeColor: PDColor,
    private val barcodeColor: PDColor,
    private val rowCount: Int,
    private val columnCount: Int,
    private val barcodeRenderSpec: BarcodeRenderSpec,
    private val shortcodeRenderSpec: ShortcodeRenderSpec,
    private val templatePageIndexToRenderCode: Int,
    private val templatePageIndexToRenderShortCode: Int
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
              .forEach { page -> renderQrCode(page.uuid, newDocument, page.pdPage) }

          pagesForCurrentBatch[templatePageIndexToRenderShortCode]
              .forEach { page -> renderShortCode(page.uuid, newDocument, page.pdPage, font) }

          pagesForCurrentBatch
              .map { renderContents -> renderContents.map { it.pdPage } }
              .forEach { PdfUtil.mergePagesIntoOne(newDocument, it, rowCount, columnCount) }
        }

    return Output(source = sourceDocument, final = newDocument)
  }

  private fun renderQrCode(uuid: UUID, document: PDDocument, page: PDPage) {
    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, barcodeRenderSpec.width, barcodeRenderSpec.height, hints)
    val bitMatrixRenderable = BitMatrixRenderable(bitMatrix, matrixScale = barcodeRenderSpec.matrixScale)

    PDPageContentStream(
        document,
        page,
        PDPageContentStream.AppendMode.APPEND,
        false
    ).use { contentStream ->

      bitMatrixRenderable.render(
          contentStream,
          barcodeRenderSpec.positionX,
          barcodeRenderSpec.positionY,
          applyForegroundColor = { it.setStrokingColor(barcodeColor) }
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
    PDPageContentStream(
        document,
        page,
        PDPageContentStream.AppendMode.APPEND,
        false
    ).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(shortCodeColor)
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
