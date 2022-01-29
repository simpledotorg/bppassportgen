package org.simple.bppassportgen.generator

sealed class PassportsGeneratorEvent

object PassportsGenerated : PassportsGeneratorEvent()

data class GeneratorTypeChanged(val generatorType: GeneratorType) : PassportsGeneratorEvent()
