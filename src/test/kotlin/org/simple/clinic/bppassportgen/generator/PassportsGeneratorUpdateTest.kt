package org.simple.clinic.bppassportgen.generator

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.bppassportgen.generator.GenerateBpPassports
import org.simple.bppassportgen.generator.GeneratePassportsButtonClicked
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.generator.GeneratorTypeChanged
import org.simple.bppassportgen.generator.NumberOfPassportsChanged
import org.simple.bppassportgen.generator.PageSize
import org.simple.bppassportgen.generator.PageSizeChanged
import org.simple.bppassportgen.generator.PassportsGeneratorModel
import org.simple.bppassportgen.generator.PassportsGeneratorUpdate
import org.simple.bppassportgen.generator.TemplateFileSelected

class PassportsGeneratorUpdateTest {

  private val defaultModel = PassportsGeneratorModel.create()
  private val updateSpec = UpdateSpec(PassportsGeneratorUpdate())

  @Test
  fun `when generator type is changed, then update the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(GeneratorTypeChanged(GeneratorType.Sticker))
        .then(assertThatNext(
            hasModel(defaultModel.generatorTypeChanged(GeneratorType.Sticker)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when generator type is changed, then update the generator type and reset page size`() {
    updateSpec
      .given(defaultModel.pageSizeChanged(pageSize = PageSize(
        rows = 1,
        columns = 2,
        label = "A4",
        labelWithCount = "2 passports per page"
      )))
      .whenEvent(GeneratorTypeChanged(GeneratorType.Sticker))
      .then(assertThatNext(
        hasModel(defaultModel.generatorTypeChanged(GeneratorType.Sticker)),
        hasNoEffects()
      ))
  }

  @Test
  fun `when the template file path is selected, then update the model`() {
    val templateFilePath = "/template.pdf"

    updateSpec
        .given(defaultModel)
        .whenEvent(TemplateFileSelected(templateFilePath))
        .then(assertThatNext(
            hasModel(defaultModel.templateFileSelected(templateFilePath)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the number of passports count is changed, then update the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(NumberOfPassportsChanged(10))
        .then(assertThatNext(
            hasModel(defaultModel.numberOfPassportChanged(10)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when generate passports button is clicked, then generate the passports`() {
    val model = defaultModel
        .generatorTypeChanged(GeneratorType.Passport)
        .templateFileSelected("/template.pdf")
        .numberOfPassportChanged(10)
        .pageSizeChanged(PageSize(
          rows = 1,
          columns = 2,
          label = "A4",
          labelWithCount = "2 passports per page"
        ))

    updateSpec
        .given(model)
        .whenEvent(GeneratePassportsButtonClicked("/output"))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GenerateBpPassports(
                templateFilePath = "/template.pdf",
                outputDirectoryPath = "/output",
                numberOfPassports = 10,
                rowCount = 1,
                columnCount = 2,
                generatorType = GeneratorType.Passport
            ))
        ))
  }

  @Test
  fun `when page size is changed, then update the model`() {
    updateSpec
      .given(defaultModel)
      .whenEvent(PageSizeChanged(PageSize(
        rows = 1,
        columns = 2,
        label = "A4",
        labelWithCount = "2 passports per page"
      )))
      .then(assertThatNext(
        hasModel(defaultModel.pageSizeChanged(PageSize(
          rows = 1,
          columns = 2,
          label = "A4",
          labelWithCount = "2 passports per page"
        ))),
        hasNoEffects()
      ))
  }
}
