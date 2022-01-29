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

class PassportsGeneratorUpdateTest {

  @Test
  fun `when generator type is changed, then update the model`() {
    val defaultModel = PassportsGeneratorModel.create()

    UpdateSpec(PassportsGeneratorUpdate())
        .given(defaultModel)
        .whenEvent(GeneratorTypeChanged(GeneratorType.Sticker))
        .then(assertThatNext(
            hasModel(defaultModel.generatorTypeChanged(GeneratorType.Sticker)),
            hasNoEffects()
        ))
  }
}
