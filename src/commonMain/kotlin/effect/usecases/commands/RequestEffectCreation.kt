package effect.usecases.commands

import effect.domain.Effect
import effect.domain.EffectRepository
import shared.domain.EventBus

class RequestEffectCreation(
    private val effectRepository: EffectRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(effectDto: Effect.Dto) {
        if(effectRepository.searchById(effectDto.id) != null) return
        val (events, effect) = Effect.create(effectDto).pullEvents()
        effectRepository.create(effect)
        eventBus.publish(events)
    }
}
