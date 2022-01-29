package org.simple.clinic.bppassportgen.generator

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.bppassportgen.generator.GeneratorType
import org.simple.bppassportgen.generator.GeneratorTypeChanged
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
}
