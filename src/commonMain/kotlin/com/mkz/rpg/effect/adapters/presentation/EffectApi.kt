package com.mkz.rpg.effect.adapters.presentation

import com.mkz.rpg.effect.adapters.events.OnRequestEffectCreation
import com.mkz.rpg.effect.adapters.events.OnRequestInitialiseEffects
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.usecases.commands.InitialiseEffects
import com.mkz.rpg.effect.usecases.commands.RequestEffectCreation
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.effect.usecases.services.EffectLoader
import com.mkz.rpg.shared.domain.EventBus

class EffectApi(
    eventBus: EventBus,
    private val effectLoader: EffectLoader,
) {
    // Storage
    private val effectRepository = InMemoryEffectRepository()

    // Commands
    val requestEffectCreation = RequestEffectCreation(effectRepository, eventBus)
    val initialiseEffects = InitialiseEffects(effectLoader, effectRepository, eventBus)

    // Queries
    val searchEffectById = SearchEffectById(effectRepository)

    // Events
    private val onRequestEffectCreation = OnRequestEffectCreation(requestEffectCreation, eventBus)
    private val onRequestInitialiseEffects = OnRequestInitialiseEffects(initialiseEffects, eventBus)

    suspend fun init() {
        effectLoader.initResources()
    }
}
