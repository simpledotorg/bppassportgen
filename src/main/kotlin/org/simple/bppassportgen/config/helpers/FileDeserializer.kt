package org.simple.bppassportgen.config.helpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import java.io.File

class FileDeserializer : StdDeserializer<File>(File::class.java) {

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): File {
    return with(p.codec.readTree<TextNode>(p)) { File(textValue()) }
  }
}
