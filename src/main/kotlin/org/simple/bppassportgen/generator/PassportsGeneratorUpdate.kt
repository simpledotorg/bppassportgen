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
      is OutputDirectorySelected -> next(model.outputDirectorySelected(event.outputDirectoryPath))
      is NumberOfPassportsChanged -> next(model.numberOfPassportChanged(event.numberOfPassports))
      is RowCountChanged -> next(model.rowCountChanged(event.rowCount))
      is ColumnCountChanged -> next(model.columnCountChanged(event.columnCount))
      GeneratePassportsButtonClicked -> dispatch(setOf(GenerateBpPassports(
          templateFilePath = model.templateFilePath!!,
          outputDirectoryPath = model.outputDirectoryPath!!,
          numberOfPassports = model.numberOfPassports!!,
          rowCount = model.rowCount!!,
          columnCount = model.columnCount!!
      )))
    }
  }
}
