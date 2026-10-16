package com.mkz.rpg.ability.adapters.presentation

import com.mkz.rpg.ability.adapters.events.OnRequestAbilityCreation
import com.mkz.rpg.ability.adapters.events.OnRequestInitialiseAbilities
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.ability.usecases.commands.InitialiseAbilities
import com.mkz.rpg.ability.usecases.commands.RequestAbilityCreation
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.ability.usecases.services.AbilityLoader
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.shared.domain.EventBus

class AbilityApi(
    effectApi: EffectApi,
    eventBus: EventBus,
    private val abilityLoader: AbilityLoader = ResourcesAbilityLoader(),
) {
    // Storage
    private val abilityRepository: AbilityRepository = InMemoryAbilityRepository()

    // Commands
    val requestAbilityCreation = RequestAbilityCreation(abilityRepository, effectApi.searchEffectById, eventBus)
    val initialiseAbilities = InitialiseAbilities(abilityLoader, abilityRepository, effectApi.searchEffectById, eventBus)

    // Queries
    val searchAbilityById = SearchAbilityById(abilityRepository)

    // Events
    private val onRequestAbilityCreation = OnRequestAbilityCreation(requestAbilityCreation, eventBus)
    private val onRequestInitialiseAbilities = OnRequestInitialiseAbilities(initialiseAbilities, eventBus)

    suspend fun init() {
        abilityLoader.initResources()
    }
}
