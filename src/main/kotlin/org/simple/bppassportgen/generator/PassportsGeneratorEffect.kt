package org.simple.bppassportgen.generator

import java.util.UUID

sealed class PassportsGeneratorEffect

data class GenerateBpPassports(
    val templateFilePath: String,
    val outputDirectoryPath: String,
    val rowCount: Int,
    val columnCount: Int,
    val uuidsToGenerate: List<UUID>
) : PassportsGeneratorEffect()
