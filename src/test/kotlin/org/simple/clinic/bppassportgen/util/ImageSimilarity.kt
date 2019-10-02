package org.simple.clinic.bppassportgen.util

import java.awt.image.BufferedImage
import kotlin.math.abs

/**
 * Method was copied from [RosettaCode](https://rosettacode.org/wiki/Percentage_difference_between_images#Kotlin)
 **/
object ImageSimilarity {

  fun differenceBetween(img1: BufferedImage, img2: BufferedImage): Double {
    val width = img1.width
    val height = img1.height
    val width2 = img2.width
    val height2 = img2.height
    if (width != width2 || height != height2) {
      val f = "(%d,%d) vs. (%d,%d)".format(width, height, width2, height2)
      throw IllegalArgumentException("Images must have the same dimensions: $f")
    }
    var diff = 0L
    for (y in 0 until height) {
      for (x in 0 until width) {
        diff += pixelDiff(img1.getRGB(x, y), img2.getRGB(x, y))
      }
    }
    val maxDiff = 3L * 255 * width * height
    return diff / maxDiff.toDouble()
  }

  fun similarity(img1: BufferedImage, img2: BufferedImage): Double {
    return 1.0 - differenceBetween(img1, img2)
  }

  private fun pixelDiff(rgb1: Int, rgb2: Int): Int {
    val r1 = (rgb1 shr 16) and 0xff
    val g1 = (rgb1 shr 8) and 0xff
    val b1 = rgb1 and 0xff
    val r2 = (rgb2 shr 16) and 0xff
    val g2 = (rgb2 shr 8) and 0xff
    val b2 = rgb2 and 0xff
    return abs(r1 - r2) + abs(g1 - g2) + abs(b1 - b2)
  }
}
