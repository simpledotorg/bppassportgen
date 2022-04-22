package org.simple.bppassportgen.generator

data class PassportsGeneratorModel(
  val generatorType: GeneratorType,
  val templateFilePath: String?,
  val outputDirectoryPath: String?,
  val numberOfPassports: Int?,
  val rowCount: Int?,
  val columnCount: Int?,
  val pageSize: PageSize
) {

  companion object {
    fun create() = PassportsGeneratorModel(
      generatorType = GeneratorType.Passport,
      templateFilePath = null,
      outputDirectoryPath = null,
      numberOfPassports = null,
      rowCount = null,
      columnCount = null,
      pageSize = PageSize.A4
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

  fun rowCountChanged(rowCount: Int): PassportsGeneratorModel {
    return copy(rowCount = rowCount)
  }

  fun columnCountChanged(columnCount: Int): PassportsGeneratorModel {
    return copy(columnCount = columnCount)
  }

  fun pageSizeChanged(pageSize: PageSize): PassportsGeneratorModel {
    return copy(pageSize = pageSize)
  }
}
