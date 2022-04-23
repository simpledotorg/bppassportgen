package org.simple.bppassportgen

import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
import org.apache.pdfbox.util.Matrix

object PdfUtil {

  fun clone(pdPage: PDPage): PDPage {
    val pageDictionary = pdPage.cosObject
    val clonedDictionary = COSDictionary(pageDictionary)

    return PDPage(clonedDictionary)
  }

  fun mergePagesIntoOne(document: PDDocument, pdPages: List<PDPage>, rowCount: Int, columnCount: Int): PDPage {
    val targetRectangle = pdPages
      .first()
      .mediaBox
      .let { sourceRectangle ->
        PDRectangle(sourceRectangle.width * columnCount, sourceRectangle.height * rowCount)
      }

    val target = PDPage(targetRectangle)

    target.resources = PDResources()
    document.addPage(target)

    val pageMatrix = pdPages
      .map { page -> asXObject(document, page) }
      .toMutableList()
      .let { pageXObjects ->
        val pageMatrix: MutableList<MutableList<PDFormXObject?>> = mutableListOf()
        (0 until rowCount).forEach { _ ->
          pageMatrix.add(MutableList(columnCount) { null })
        }

        (0 until columnCount).forEach { columnIndex ->
          (0 until rowCount).forEach { rowIndex ->
            pageMatrix[rowIndex][columnIndex] = if (pageXObjects.isNotEmpty()) pageXObjects.removeAt(0) else null
          }
        }

        pageMatrix
      }

    streamForPage(document, target).use { contentStream ->

      val (pageWidth, pageHeight) = pdPages
        .first()
        .mediaBox
        .let { sourceRectangle ->
          sourceRectangle.width to sourceRectangle.height
        }

      pageMatrix.forEach { row ->

        row
          .filterNotNull()
          .forEach { xObject ->
            target.resources.add(xObject)
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

  fun streamForPage(
    document: PDDocument,
    page: PDPage,
    appendMode: AppendMode = AppendMode.APPEND,
    compress: Boolean = false
  ): PDPageContentStream {
    return PDPageContentStream(document, page, appendMode, compress)
  }
}
