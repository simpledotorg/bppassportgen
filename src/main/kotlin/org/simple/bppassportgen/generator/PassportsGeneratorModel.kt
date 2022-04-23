package org.simple.bppassportgen.generator

data class PassportsGeneratorModel(
  val generatorType: GeneratorType,
  val templateFilePath: String?,
  val outputDirectoryPath: String?,
  val numberOfPassports: Int?,
  val pageSize: PageSize_Old?
) {

  companion object {
    fun create() = PassportsGeneratorModel(
      generatorType = GeneratorType.Passport,
      templateFilePath = null,
      outputDirectoryPath = null,
      numberOfPassports = null,
      pageSize = null
    )
  }

  fun generatorTypeChanged(type: GeneratorType): PassportsGeneratorModel {
    return copy(generatorType = type)
  }

  fun templateFileSelected(templateFilePath: String): PassportsGeneratorModel {
    return copy(templateFilePath = templateFilePath)
  }

  fun outputDirectorySelected(outputDirectoryPath: String): PassportsGeneratorModel {
    return copy(outputDirectoryPath = outputDirectoryPath)
  }

  fun numberOfPassportChanged(numberOfPassports: Int): PassportsGeneratorModel {
    return copy(numberOfPassports = numberOfPassports)
  }

  fun pageSizeChanged(pageSize: PageSize_Old): PassportsGeneratorModel {
    return copy(pageSize = pageSize)
  }
}
