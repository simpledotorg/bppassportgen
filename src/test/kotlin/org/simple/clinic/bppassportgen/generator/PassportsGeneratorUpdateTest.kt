package org.simple.clinic.bppassportgen.generator

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.generator.GeneratorTypeChanged
import org.simple.bppassportgen.generator.NumberOfPassportsChanged
import org.simple.bppassportgen.generator.OutputDirectorySelected
import org.simple.bppassportgen.generator.PassportsGeneratorModel
import org.simple.bppassportgen.generator.PassportsGeneratorUpdate
import org.simple.bppassportgen.generator.RowCountChanged
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
  fun `when the output director is selected, then update the model`() {
    val outputDirectoryPath = "/passports"

    updateSpec
        .given(defaultModel)
        .whenEvent(OutputDirectorySelected(outputDirectoryPath))
        .then(assertThatNext(
            hasModel(defaultModel.outputDirectorySelected(outputDirectoryPath)),
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
  fun `when row count is changed, then update the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RowCountChanged(2))
        .then(assertThatNext(
            hasModel(defaultModel.rowCountChanged(2)),
            hasNoEffects()
        ))
  }
}
