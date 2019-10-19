package org.simple.clinic.bppassportgen.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import junitparams.JUnitParamsRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.bppassportgen.config.helpers.FileDeserializer
import strikt.api.expect
import strikt.assertions.isEqualTo
import java.io.File
import java.nio.file.Path

@RunWith(JUnitParamsRunner::class)
class FileDeserializerTest {

  private val mapper = ObjectMapper().apply {
    val module = SimpleModule()
    module.addDeserializer(File::class.java, FileDeserializer())
    registerModule(module)
  }

  @Test
  fun `file must deserialize as expected`() {
    // given
    val firstFilePath = Path.of("a", "b", "c").toString()
    val secondFilePath = Path.of("d", "e").toAbsolutePath().toString()
    val json = """
      |{
      | "first": "$firstFilePath",
      | "second": "$secondFilePath"
      |}
    """.trimMargin()

    // when
    val type = object : TypeReference<Map<String, File>>() {}
    val files = mapper.readValue(json, type)

    // then
    expect {
      that(files.getValue("first").path).isEqualTo(firstFilePath)
      that(files.getValue("second").path).isEqualTo(secondFilePath)
    }
  }
}
