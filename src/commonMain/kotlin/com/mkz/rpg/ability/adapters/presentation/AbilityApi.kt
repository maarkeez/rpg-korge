package com.mkz.rpg.ability.adapters.presentation

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.ability.usecases.commands.RequestAbilityCreation
import com.mkz.rpg.ability.usecases.queries.CalculateImmediateDamage
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.shared.domain.EventBus

class AbilityApi(
    effectApi: EffectApi,
    eventBus: EventBus,
) {
    // Storage
    private val abilityRepository: AbilityRepository = InMemoryAbilityRepository()

    // Commands
    val requestAbilityCreation = RequestAbilityCreation(abilityRepository, effectApi.searchEffectById, eventBus)

    // Queries
    val searchAbilityById = SearchAbilityById(abilityRepository)
    val calculateImmediateDamage = CalculateImmediateDamage(abilityRepository, effectApi.searchEffectById)
}
