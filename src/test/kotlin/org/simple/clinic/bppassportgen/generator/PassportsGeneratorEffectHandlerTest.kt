package org.simple.clinic.bppassportgen.generator

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.simple.bppassportgen.PassportsGenerator
import org.simple.bppassportgen.generator.GenerateBpPassports
import org.simple.bppassportgen.generator.PassportsGenerated
import org.simple.bppassportgen.generator.PassportsGeneratorEffectHandler
import org.simple.clinic.bppassportgen.util.EffectHandlerTestCase
import java.io.File
import java.util.UUID

class PassportsGeneratorEffectHandlerTest {

  private val passportsGenerator = mock<PassportsGenerator>()

  private val effectHandler = PassportsGeneratorEffectHandler(passportsGenerator)
      .build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @Test
  fun `when generate bp passports effect is received, then generate the passports`() {
    // given
    val uuidsToGenerate = listOf(
        UUID.fromString("b6bf0aaf-960e-4389-b683-790cf960933d"),
        UUID.fromString("1c287cdf-7439-41a6-807f-c015a8d39fca"),
        UUID.fromString("67710382-cc4d-4b1e-91ac-2ec4594d42ce")
    )

    // when
    effectHandlerTestCase.dispatch(GenerateBpPassports(
        templateFilePath = "/template-file.pdf",
        outputDirectoryPath = "/passports",
        rowCount = 2,
        columnCount = 2,
        uuidsToGenerate = uuidsToGenerate
    ))

    // then
    verify(passportsGenerator).run(
        uuidsToGenerate = uuidsToGenerate,
        rowCount = 2,
        columnCount = 2,
        templateFilePath = "/template-file.pdf",
        outputDirectory = File("/passports")
    )

    effectHandlerTestCase.assertOutgoingEvents(PassportsGenerated)
  }
}
