package org.simple.bppassportgen.generator

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class PassportsGeneratorUpdate : Update<PassportsGeneratorModel, PassportsGeneratorEvent, PassportsGeneratorEffect> {

  override fun update(model: PassportsGeneratorModel, event: PassportsGeneratorEvent): Next<PassportsGeneratorModel, PassportsGeneratorEffect> {
    return when (event) {
      is GeneratorTypeChanged -> next(model.generatorTypeChanged(event.generatorType))
      // TODO (SM): Reset state once passports are generator progress state
      PassportsGenerated -> noChange()
      is TemplateFileSelected -> next(model.templateFileSelected(event.templateFilePath))
      is NumberOfPassportsChanged -> next(model.numberOfPassportChanged(event.numberOfPassports))
      is GeneratePassportsButtonClicked -> {
        requireNotNull(model.pageSize)

        dispatch(setOf(GenerateBpPassports(
          templateFilePath = model.templateFilePath!!,
          outputDirectoryPath = event.outputDirectory,
          numberOfPassports = model.numberOfPassports!!,
          rowCount = model.pageSize.rows,
          columnCount = model.pageSize.columns,
          generatorType = model.generatorType
        )))
      }
      is PageSizeChanged -> next(model.pageSizeChanged(event.pageSize))
    }
  }
}
