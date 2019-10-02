package org.simple.clinic.bppassportgen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage

object SavePdfToImage {

  fun save(document: PDDocument): List<BufferedImage> {
    val renderer = PDFRenderer(document)

    return (0 until document.numberOfPages).map { renderer.renderImageWithDPI(it, 150F, ImageType.GRAY) }
  }
}
