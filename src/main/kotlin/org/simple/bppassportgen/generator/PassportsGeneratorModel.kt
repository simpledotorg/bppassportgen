package org.simple.bppassportgen.generator

data class PassportsGeneratorModel(
    val generatorType: GeneratorType,
    val templateFilePath: String?,
    val outputDirectoryPath: String?,
    val numberOfPassports: Int?
) {

  companion object {
    fun create() = PassportsGeneratorModel(
        generatorType = GeneratorType.Passport,
        templateFilePath = null,
        outputDirectoryPath = null,
        numberOfPassports = null
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
}
