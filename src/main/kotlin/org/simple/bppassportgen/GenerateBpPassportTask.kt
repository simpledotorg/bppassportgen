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
    val taskNumber: Int,
    val pdfBytes: ByteArray,
    val fontBytes: ByteArray,
    val uuidBatches: List<List<UUID>>,
    val qrCodeWriter: QRCodeWriter,
    val hints: Map<EncodeHintType, Any>,
    val shortCodeColor: PDColor,
    val barcodeColor: PDColor,
    val rowCount: Int,
    val columnCount: Int
) : Callable<Output> {

  override fun call(): Output {
    return PDDocument.load(pdfBytes)
        .let { document ->
          val frontPage = document.getPage(0)
          val backPage = document.getPage(1)

          val font = PDType0Font.load(document, ByteArrayInputStream(fontBytes))

          val newDocument = PDDocument()

          uuidBatches
              .forEach { uuids ->
                val frontPages = uuids
                    .map { uuid ->
                      Page(
                          uuid = uuid,
                          page = frontPage
                              .clone()
                              .apply {
                                renderBpPassportCodeOnPage(this, newDocument, font, uuid)
                              }
                      )
                    }

                val backPages = frontPages
                    .map { (id, _) -> Page(uuid = id, page = backPage.clone()) }

                mergePages(newDocument, frontPages, rowCount, columnCount)
                mergePages(newDocument, backPages, rowCount, columnCount)
              }

          Output(source = document, final = newDocument)
        }
  }

  private fun renderBpPassportCodeOnPage(
      page: PDPage,
      document: PDDocument,
      font: PDType0Font,
      uuid: UUID
  ) {
    val shortCode = shortCodeForUuid(uuid)
    val bitMatrix = qrCodeWriter.encode(uuid.toString(), BarcodeFormat.QR_CODE, 80, 80, hints)
    val bitMatrixRenderable = BitMatrixRenderable(bitMatrix)

    PDPageContentStream(
        document,
        page,
        PDPageContentStream.AppendMode.APPEND,
        false
    ).use { contentStream ->
      contentStream.beginText()
      contentStream.setNonStrokingColor(shortCodeColor)
      contentStream.newLineAtOffset(72.5F, 210F)
      contentStream.setCharacterSpacing(2.4F)
      contentStream.setFont(font, 12F)
      contentStream.showText(shortCode)
      contentStream.endText()

      bitMatrixRenderable.render(
          contentStream,
          196F,
          107.5F,
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
