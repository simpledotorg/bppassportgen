package org.simple.bppassportgen

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.renderable.RenderSpecProviderImpl
import org.simple.bppassportgen.renderable.Renderable.Type.PassportQrCode
import org.simple.bppassportgen.renderable.Renderable.Type.PassportShortcode
import org.simple.bppassportgen.renderable.qrcode.BarcodeRenderSpec
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderSpec
import java.io.File
import java.util.UUID

fun main(args: Array<String>) {
  val options = Options()
      .apply {
        addRequiredOption("c", "count", true, "Number of BP Passports to generate")
        addRequiredOption("t", "template", true, "Path to the template file")
        addOption("o", "output", true, "Directory to save the generated BP passports")
        addOption("rc", "row-count", true, "Number of rows in a page")
        addOption("cc", "column-count", true, "Number of columns in a page")
        addOption("h", "help", false, "Print this message")
        addOption("sticker", false, "Generate stickers instead of the BP Passports")
      }

  val helpFormatter = HelpFormatter()

  if (args.isEmpty()) {
    helpFormatter.printHelp("bppassportgen", options)
  } else {

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    if (cmd.hasOption("h")) {
      helpFormatter.printHelp("bppassportgen", options)
    } else {
      val numberOfPassports = cmd.getOptionValue("c").toInt()
      val templateFilePath = cmd.getOptionValue("t")
      val outDirectory = File(cmd.getOptionValue("o", "./out"))
      val rowCount = cmd.getOptionValue("rc", "1").toInt()
      val columnCount = cmd.getOptionValue("cc", "1").toInt()
      val isSticker = cmd.hasOption("sticker")

      require(numberOfPassports > 0) { "Number of passports must be > 0!" }
      require(rowCount * columnCount <= numberOfPassports) { "row count * column count of passports must be <= count!" }

      val metropolisFontId = "Metropolis-Medium"
      val blackCmykId = "cmyk_black"

      val uuidsToGenerate = (0 until numberOfPassports).map { UUID.randomUUID() }
      val renderSpecs = listOf(
          RenderableSpec(0, PassportQrCode, if (isSticker) {
            BarcodeRenderSpec(width = 80, height = 80, matrixScale = 0.85F, positionX = 4.5F, positionY = 17F, colorId = blackCmykId)
          } else {
            BarcodeRenderSpec(width = 80, height = 80, matrixScale = 1.35F, positionX = 196F, positionY = 107.5F, colorId = blackCmykId)
          }),
          RenderableSpec(0, PassportShortcode, if (isSticker) {
            ShortcodeRenderSpec(positionX = 16F, positionY = 8F, fontSize = 8F, characterSpacing = 1.2F, fontId = metropolisFontId, colorId = blackCmykId)
          } else {
            ShortcodeRenderSpec(positionX = 88F, positionY = 210F, fontSize = 12F, characterSpacing = 2.4F, fontId = metropolisFontId, colorId = blackCmykId)
          })
      )
      val fonts = mapOf(metropolisFontId to "Metropolis-Medium.ttf")
      val colors = mapOf(blackCmykId to PDColor(
          floatArrayOf(0F, 0F, 0F, 1F),
          COSName.DEVICECMYK,
          PDDeviceCMYK.INSTANCE
      ))

      PassportsGenerator(
          fonts = fonts,
          renderSpecs = renderSpecs,
          renderSpecProvider = RenderSpecProviderImpl(),
          colorMap = colors
      ).run(
          uuidsToGenerate = uuidsToGenerate,
          rowCount = rowCount,
          columnCount = columnCount,
          templateFilePath = templateFilePath,
          outputDirectory = outDirectory,
          generatorType = if (isSticker) GeneratorType.Sticker else GeneratorType.Passport
      )
    }
  }
}
