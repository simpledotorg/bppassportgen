package org.simple.bppassportgen

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.simple.bppassportgen.consoleprinter.ConsolePrinter
import org.simple.bppassportgen.consoleprinter.RealConsolePrinter
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.progresspoll.ProgressPoll
import org.simple.bppassportgen.progresspoll.RealProgressPoll
import org.simple.bppassportgen.qrcodegen.QrCodeGenerator
import org.simple.bppassportgen.qrcodegen.QrCodeGeneratorImpl
import org.simple.bppassportgen.renderable.RenderSpecProvider
import org.simple.bppassportgen.renderable.Renderable
import org.simple.bppassportgen.renderable.Renderable.Type.PassportQrCode
import org.simple.bppassportgen.renderable.Renderable.Type.PassportShortcode
import org.simple.bppassportgen.renderable.qrcode.QrCodeRenderable
import org.simple.bppassportgen.renderable.shortcode.ShortcodeRenderable
import java.io.File
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class PassportsGenerator(
    private val computationThreadPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
    private val ioThreadPool: ExecutorService = Executors.newCachedThreadPool(),
    private val progressPoll: ProgressPoll = RealProgressPoll(Duration.ofSeconds(1)),
    private val consolePrinter: ConsolePrinter = RealConsolePrinter(),
    private val fonts: Map<String, String>,
    private val renderSpecs: List<RenderableSpec>,
    private val renderSpecProvider: RenderSpecProvider,
    private val colorMap: Map<String, PDColor>,
    private val pageCount: Int = 100
) {

  fun run(
      uuidsToGenerate: List<UUID>,
      rowCount: Int,
      columnCount: Int,
      templateFilePath: String,
      outputDirectory: File,
      generatorType: GeneratorType
  ) {
    val mergeCount = rowCount * columnCount

    outputDirectory.mkdirs()

    val pdfTemplateFile = File(templateFilePath)
    val pdfInputBytes = pdfTemplateFile.readBytes()

    val uuidBatches = uuidsToGenerate
        .distinct()
        .windowed(size = mergeCount, step = mergeCount, partialWindows = true)
        .windowed(size = pageCount, step = pageCount, partialWindows = true)

    val generatingPdfTasks = mutableMapOf<Int, Future<Output>>()
    val savePdfTasks = mutableListOf<Future<Any>>()

    val qrCodeGenerator: QrCodeGenerator = QrCodeGeneratorImpl(errorCorrectionLevel = ErrorCorrectionLevel.Q, margin = 0)
    val documentFactory = PdDocumentFactory(
        fontsToLoad = fonts
            .mapValues { (_, fontName) -> javaClass.classLoader.getResourceAsStream(fontName)!!.readBytes() }
    )
    val colorProvider = ColorProvider(colorMap)

    uuidBatches
        .mapIndexed { index, uuidBatch ->
          val pageSpecs = uuidBatch
              .map { uuidsInEachPage ->
                uuidsInEachPage.map { uuid ->
                  PageSpec(generateRenderables(qrCodeGenerator, uuid, colorProvider, renderSpecProvider.renderSpecs(generatorType)))
                }
              }
              .toList()

          val task = createPassportGenerationTask(
              pdfInputBytes = pdfInputBytes,
              rowCount = rowCount,
              columnCount = columnCount,
              pageSpecs = pageSpecs,
              documentFactory = documentFactory
          )

          task to index + 1
        }
        .forEach { (task, taskNumber) ->
          generatingPdfTasks[taskNumber] = computationThreadPool.submit(task)
        }

    var tasksComplete = false
    while (tasksComplete.not()) {
      val generated = generatingPdfTasks.filter { (_, future) -> future.isDone }

      generated
          .map { it.key to it.value.get() }
          .map { (taskNumber, output) ->
            SaveBpPassportTask(
                output = output,
                taskNumber = taskNumber,
                totalSize = uuidBatches.size,
                directory = outputDirectory
            )
          }
          .forEach {
            savePdfTasks += ioThreadPool.submit(it, Any())
          }

      generated
          .map { it.key }
          .forEach { generatingPdfTasks.remove(it) }

      val numberOfSavedPdfs = savePdfTasks.count { it.isDone }
      consolePrinter.print("Finished $numberOfSavedPdfs/${uuidBatches.size} Passports!")

      if (savePdfTasks.size == uuidBatches.size && savePdfTasks.all { it.isDone }) {
        tasksComplete = true
      }
      progressPoll.poll()
    }

    computationThreadPool.shutdown()
    ioThreadPool.shutdown()
  }

  private fun generateRenderables(
      qrCodeGenerator: QrCodeGenerator,
      uuid: UUID,
      colorProvider: ColorProvider,
      renderSpecs: List<RenderableSpec>
  ): Map<Int, List<Renderable>> {
    return renderSpecs
        .map { it.pageNumber to generateRenderable(uuid, qrCodeGenerator, it, colorProvider) }
        .groupBy({ (pageNumber, _) -> pageNumber }, { (_, renderable) -> renderable })
  }

  private fun generateRenderable(
      uuid: UUID,
      qrCodeGenerator: QrCodeGenerator,
      spec: RenderableSpec,
      colorProvider: ColorProvider
  ): Renderable {
    return when (spec.type) {
      PassportQrCode -> QrCodeRenderable(qrCodeGenerator, uuid, spec.getSpecAs(), colorProvider)
      PassportShortcode -> ShortcodeRenderable(uuid, spec.getSpecAs(), colorProvider)
    }
  }

  private fun createPassportGenerationTask(
      pdfInputBytes: ByteArray,
      rowCount: Int,
      columnCount: Int,
      pageSpecs: List<List<PageSpec>>,
      documentFactory: PdDocumentFactory
  ): Callable<Output> {
    return GenerateBpPassportTask(
        pdfBytes = pdfInputBytes,
        rowCount = rowCount,
        columnCount = columnCount,
        pageSpecs = pageSpecs,
        documentFactory = documentFactory
    )
  }
}
