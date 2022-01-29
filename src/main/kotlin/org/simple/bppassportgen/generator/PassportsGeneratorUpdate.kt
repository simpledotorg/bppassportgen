package org.simple.bppassportgen.generator

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class PassportsGeneratorUpdate : Update<PassportsGeneratorModel, PassportsGeneratorEvent, PassportsGeneratorEffect> {

  override fun update(model: PassportsGeneratorModel, event: PassportsGeneratorEvent): Next<PassportsGeneratorModel, PassportsGeneratorEffect> {
    return when (event) {
      is GeneratorTypeChanged -> next(model.generatorTypeChanged(event.generatorType))
      // TODO (SM): Reset state once passports are generator progress state
      PassportsGenerated -> noChange()
    }
  }
}
