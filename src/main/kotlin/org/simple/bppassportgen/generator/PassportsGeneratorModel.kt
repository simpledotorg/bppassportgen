package org.simple.bppassportgen.generator

data class PassportsGeneratorModel(
    val generatorType: GeneratorType,
    val templateFilePath: String?
) {

  companion object {
    fun create() = PassportsGeneratorModel(
        generatorType = GeneratorType.Passport,
        templateFilePath = null
    )
  }

  fun generatorTypeChanged(type: GeneratorType): PassportsGeneratorModel {
    return copy(generatorType = type)
  }

  fun templateFileSelected(templateFilePath: String): PassportsGeneratorModel {
    return copy(templateFilePath = templateFilePath)
  }
}
