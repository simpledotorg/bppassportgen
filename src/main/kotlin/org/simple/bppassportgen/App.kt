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
      val fonts = mapOf(metropolisFontId to "Metropolis-Medium.ttf")
      val colors = mapOf(blackCmykId to PDColor(
          floatArrayOf(0F, 0F, 0F, 1F),
          COSName.DEVICECMYK,
          PDDeviceCMYK.INSTANCE
      ))

      PassportsGenerator(
          fonts = fonts,
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
