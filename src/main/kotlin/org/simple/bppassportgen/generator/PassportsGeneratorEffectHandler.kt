package org.simple.bppassportgen.generator

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.bppassportgen.PassportsGenerator
import java.io.File

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
            passportsGenerator.run(
                uuidsToGenerate = effect.uuidsToGenerate,
                rowCount = effect.rowCount,
                columnCount = effect.columnCount,
                templateFilePath = effect.templateFilePath,
                outputDirectory = File(effect.outputDirectoryPath))
          }
          .map { PassportsGenerated }
    }
  }
}
