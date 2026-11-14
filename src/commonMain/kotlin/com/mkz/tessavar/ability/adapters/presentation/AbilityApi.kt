package com.mkz.tessavar.ability.adapters.presentation

import com.mkz.tessavar.ability.adapters.events.OnRequestAbilityCreation
import com.mkz.tessavar.ability.adapters.events.OnRequestInitialiseAbilities
import com.mkz.tessavar.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.tessavar.ability.domain.AbilityRepository
import com.mkz.tessavar.ability.usecases.commands.InitialiseAbilities
import com.mkz.tessavar.ability.usecases.commands.RequestAbilityCreation
import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.ability.usecases.services.AbilityCreation
import com.mkz.tessavar.ability.usecases.services.AbilityLoader
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.shared.domain.EventBus

class AbilityApi(
    effectApi: EffectApi,
    eventBus: EventBus,
    private val abilityLoader: AbilityLoader,
) {
    private val abilityRepository: AbilityRepository = InMemoryAbilityRepository()

    private val abilityCreation = AbilityCreation(abilityRepository, effectApi.searchEffectById)

    val requestAbilityCreation = RequestAbilityCreation(abilityCreation, eventBus)
    val initialiseAbilities = InitialiseAbilities(abilityLoader, abilityCreation, eventBus)

    val searchAbilityById = SearchAbilityById(abilityRepository)

    private val onRequestAbilityCreation = OnRequestAbilityCreation(requestAbilityCreation, eventBus)
    private val onRequestInitialiseAbilities = OnRequestInitialiseAbilities(initialiseAbilities, eventBus)

    suspend fun init() {
        abilityLoader.initResources()
    }
}
