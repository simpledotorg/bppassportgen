package org.simple.bppassportgen.generator

data class PassportsGeneratorModel(
  val generatorType: GeneratorType,
  val templateFilePath: String?,
  val numberOfPassports: Int?,
  val pageSize: PageSize?,
  val generatorProgress: GeneratorProgress
) {

  companion object {
    fun create() = PassportsGeneratorModel(
      generatorType = GeneratorType.Passport,
      templateFilePath = null,
      numberOfPassports = null,
      pageSize = null,
      generatorProgress = GeneratorProgress.INIT
    )
  }

  val isRequiredDataFilled: Boolean
    get() = templateFilePath.isNullOrBlank().not() &&
        numberOfPassports != null && numberOfPassports > 0 &&
        pageSize != null

  fun generatorTypeChanged(type: GeneratorType): PassportsGeneratorModel {
    return copy(
      generatorType = type,
      pageSize = null
    )
  }

  fun templateFileSelected(templateFilePath: String): PassportsGeneratorModel {
    return copy(templateFilePath = templateFilePath)
  }

  fun numberOfPassportChanged(numberOfPassports: Int): PassportsGeneratorModel {
    return copy(numberOfPassports = numberOfPassports)
  }

  fun pageSizeChanged(pageSize: PageSize): PassportsGeneratorModel {
    return copy(pageSize = pageSize)
  }

  fun generatorProgressChanged(generatorProgress: GeneratorProgress): PassportsGeneratorModel {
    return copy(generatorProgress = generatorProgress)
  }
}
