package org.simple.bppassportgen.generator

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.bppassportgen.PassportsGenerator

class PassportsGeneratorEffectHandler(
    private val passportsGenerator: PassportsGenerator
) {

  fun build(): ObservableTransformer<PassportsGeneratorEffect, PassportsGeneratorEvent> {
    return RxMobius
        .subtypeEffectHandler<PassportsGeneratorEffect, PassportsGeneratorEvent>()
        .addTransformer(GenerateBpPassports::class.java, generateBpPassports())
        .build()
  }

  private fun generateBpPassports(): ObservableTransformer<GenerateBpPassports, PassportsGeneratorEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { effect ->
            passportsGenerator.run(effect.uuidsToGenerate)
          }
          .map { PassportsGenerated }
    }
  }
}
