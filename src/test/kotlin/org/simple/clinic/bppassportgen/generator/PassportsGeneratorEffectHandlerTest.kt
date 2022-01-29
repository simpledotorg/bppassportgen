package org.simple.clinic.bppassportgen.generator

import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.simple.bppassportgen.PassportsGenerator
import org.simple.bppassportgen.generator.GenerateBpPassports
import org.simple.bppassportgen.generator.PassportsGenerated
import org.simple.bppassportgen.generator.PassportsGeneratorEffectHandler
import org.simple.clinic.bppassportgen.scheduler.TestSchedulersProvider
import org.simple.clinic.bppassportgen.util.EffectHandlerTestCase
import java.io.File
import java.util.UUID

class PassportsGeneratorEffectHandlerTest {

  private val passportsGenerator = mock<PassportsGenerator>()

  private val effectHandler = PassportsGeneratorEffectHandler(
      passportsGenerator = passportsGenerator,
      schedulersProvider = TestSchedulersProvider()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @Test
  fun `when generate bp passports effect is received, then generate the passports`() {
    // given
    val uuid = UUID.fromString("b6bf0aaf-960e-4389-b683-790cf960933d")

    Mockito.mockStatic(UUID::class.java)
        .`when`<UUID>(UUID::randomUUID)
        .thenReturn(uuid)

    // when
    effectHandlerTestCase.dispatch(GenerateBpPassports(
        templateFilePath = "/template-file.pdf",
        outputDirectoryPath = "/passports",
        numberOfPassports = 1,
        rowCount = 2,
        columnCount = 2
    ))

    // then
    verify(passportsGenerator).run(
        uuidsToGenerate = listOf(uuid),
        rowCount = 2,
        columnCount = 2,
        templateFilePath = "/template-file.pdf",
        outputDirectory = File("/passports")
    )

    effectHandlerTestCase.assertOutgoingEvents(PassportsGenerated)
  }
}
