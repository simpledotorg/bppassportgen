package org.simple.bppassportgen.generator

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.bppassportgen.PassportsGenerator
import org.simple.bppassportgen.scheduler.SchedulersProvider
import java.io.File
import java.util.UUID

class PassportsGeneratorEffectHandler(
  private val passportsGenerator: PassportsGenerator,
  private val schedulersProvider: SchedulersProvider
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
        .observeOn(schedulersProvider.io)
        .doOnNext { effect ->
          val uuidsToGenerate = (0 until effect.numberOfPassports).map { UUID.randomUUID() }
          passportsGenerator.run(
            uuidsToGenerate = uuidsToGenerate,
            rowCount = effect.rowCount,
            columnCount = effect.columnCount,
            templateFilePath = effect.templateFilePath,
            outputDirectory = File(effect.outputDirectoryPath),
            generatorType = effect.generatorType
          )
        }
        .map { PassportsGenerated }
    }
  }
}
