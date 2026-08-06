package effect.usecases.commands

import effect.domain.Effect
import effect.domain.EffectPublisher
import effect.domain.EffectRepository

class RequestEffectCreation(
    private val effectRepository: EffectRepository,
    private val effectPublisher: EffectPublisher,
) {
    operator fun invoke(effectDto: Effect.Dto) {
        if(effectRepository.searchById(effectDto.id) != null) return
        val (events, effect) = Effect.create(effectDto).pullEvents()
        effectRepository.create(effect)
        effectPublisher.publish(events)
    }
}
