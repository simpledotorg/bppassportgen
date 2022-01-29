package org.simple.bppassportgen.generator

data class PassportsGeneratorModel(
    val generatorType: GeneratorType
) {

  companion object {
    fun create() = PassportsGeneratorModel(
        generatorType = GeneratorType.Passport
    )
  }

  fun generatorTypeChanged(type: GeneratorType): PassportsGeneratorModel {
    return copy(generatorType = type)
  }
}
