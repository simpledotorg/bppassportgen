package org.simple.bppassportgen.generator

sealed class PassportsGeneratorEvent

object PassportsGenerated : PassportsGeneratorEvent()

data class GeneratorTypeChanged(val generatorType: GeneratorType) : PassportsGeneratorEvent()

data class TemplateFileSelected(val templateFilePath: String) : PassportsGeneratorEvent()

data class OutputDirectorySelected(val outputDirectoryPath: String) : PassportsGeneratorEvent()

data class NumberOfPassportsChanged(val numberOfPassports: Int) : PassportsGeneratorEvent()

data class GeneratePassportsButtonClicked(val outputDirectory: String) : PassportsGeneratorEvent()

data class PageSizeChanged(val pageSize: PageSize) : PassportsGeneratorEvent()
