package com.mkz.rpg.effect.adapters.presentation

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.usecases.commands.RequestEffectCreation
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class EffectApi(
    eventBus: EventBus,
) {
    // Storage
    private val effectRepository = InMemoryEffectRepository()

    // Commands
    val requestEffectCreation = RequestEffectCreation(effectRepository, eventBus)

    // Queries
    val searchEffectById = SearchEffectById(effectRepository)
}
