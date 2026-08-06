package effect.adapters.presentation

import effect.adapters.storage.InMemoryEffectRepository
import effect.usecases.commands.RequestEffectCreation
import shared.domain.EventBus

class EffectApi(eventBus: EventBus) {

    private val effectRepository = InMemoryEffectRepository()
    val requestEffectCreation = RequestEffectCreation(effectRepository, eventBus)
}
