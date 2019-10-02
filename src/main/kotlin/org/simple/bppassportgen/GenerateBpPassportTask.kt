package org.simple.bppassportgen

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
import org.apache.pdfbox.util.Matrix
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
    private val templatePageIndexToRenderCode: Int
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
              .map { sourcePage -> uuidsInOnePage.map { Page(it, sourcePage.clone()) } }

          pagesForCurrentBatch[templatePageIndexToRenderCode]
              .forEach { page ->
                renderBpPassportCodeOnPage(page.page, newDocument, font, page.uuid)
              }

          pagesForCurrentBatch.forEach { mergePages(newDocument, it, rowCount, columnCount) }
        }

    return Output(source = sourceDocument, final = newDocument)
  }

  private fun renderBpPassportCodeOnPage(
      page: PDPage,
      document: PDDocument,
      font: PDType0Font,
      uuid: UUID
  ) {
    val shortCode = shortCodeForUuid(uuid)
    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, barcodeRenderSpec.width, barcodeRenderSpec.height, hints)
    val bitMatrixRenderable = BitMatrixRenderable(bitMatrix, matrixScale = barcodeRenderSpec.matrixScale)

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

      bitMatrixRenderable.render(
          contentStream,
          barcodeRenderSpec.positionX,
          barcodeRenderSpec.positionY,
          drawBackground = false,
          applyForegroundColor = { it.setStrokingColor(barcodeColor) },
          applyBackgroundColor = { it.setStrokingColor(barcodeColor) }
      )
    }
  }

  private fun mergePages(document: PDDocument, pages: List<Page>, rowCount: Int, columnCount: Int): PDPage {
    val targetRectangle = pages
        .first()
        .page
        .mediaBox
        .let { sourceRectangle ->
          PDRectangle(sourceRectangle.width * columnCount, sourceRectangle.height * rowCount)
        }

    val target = PDPage(targetRectangle)

    target.resources = PDResources()
    document.addPage(target)

    val pageMatrix = pages
        .map { page -> page.uuid to asXObject(document, page.page) }
        .toMutableList()
        .let { pageXObjects ->
          val pageMatrix: MutableList<MutableList<Pair<UUID, PDFormXObject>?>> = mutableListOf()
          (0 until rowCount).forEach { rowIndex ->
            pageMatrix.add(MutableList(columnCount) { null })
          }

          (0 until columnCount).forEach { columnIndex ->
            (0 until rowCount).forEach { rowIndex ->
              pageMatrix[rowIndex][columnIndex] = if (pageXObjects.isNotEmpty()) pageXObjects.removeAt(0) else null
            }
          }

          pageMatrix
        }

    PDPageContentStream(
        document,
        target,
        PDPageContentStream.AppendMode.APPEND,
        false
    ).use { contentStream ->

      val (pageWidth, pageHeight) = pages
          .first()
          .page
          .mediaBox
          .let { sourceRectangle ->
            sourceRectangle.width to sourceRectangle.height
          }

      pageMatrix.forEach { row ->

        row
            .filter { it != null }
            .forEach { page ->
              val (id, xObject) = page!!
              target.resources.add(xObject, id.toString())
              contentStream.drawForm(xObject)
              contentStream.transform(Matrix.getTranslateInstance(pageWidth, 0F))
            }

        contentStream.transform(Matrix.getTranslateInstance(-pageWidth * columnCount, pageHeight))
      }
    }

    return target
  }

  private fun asXObject(document: PDDocument, page: PDPage): PDFormXObject {
    val xObject = PDFormXObject(document)

    xObject.stream.createOutputStream().use { outputStream ->
      page.contents.use { inputStream ->
        inputStream.copyTo(outputStream)
      }
    }

    xObject.resources = page.resources
    xObject.bBox = page.cropBox

    return xObject
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

  private fun PDPage.clone(): PDPage {
    val pageDictionary = this.cosObject
    val clonedDictionary = COSDictionary(pageDictionary)

    return PDPage(clonedDictionary)
  }

  private data class Page(val uuid: UUID, val page: PDPage)
}
