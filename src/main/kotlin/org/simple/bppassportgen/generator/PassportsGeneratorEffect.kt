package org.simple.bppassportgen.generator

sealed class PassportsGeneratorEffect

data class GenerateBpPassports(
    val templateFilePath: String,
    val outputDirectoryPath: String,
    val numberOfPassports: Int,
    val rowCount: Int,
    val columnCount: Int,
    val generatorType: GeneratorType
) : PassportsGeneratorEffect()
