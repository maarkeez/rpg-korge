package effect.adapters.presentation

import effect.adapters.storage.InMemoryEffectRepository
import effect.usecases.commands.RequestEffectCreation
import effect.usecases.queries.SearchEffectById
import shared.domain.EventBus

class EffectApi(eventBus: EventBus) {

    // Storage
    private val effectRepository = InMemoryEffectRepository()

    // Commands
    val requestEffectCreation = RequestEffectCreation(effectRepository, eventBus)

    // Queries
    val searchEffectById = SearchEffectById(effectRepository)
}
