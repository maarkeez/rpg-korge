package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.ability.usecases.services.AbilityCreation
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class RequestAbilityCreation(
    abilityRepository: AbilityRepository,
    searchEffectById: SearchEffectById,
    private val eventBus: EventBus,
) {
    private val abilityCreation = AbilityCreation(abilityRepository, searchEffectById)

    operator fun invoke(abilityDto: Ability.Dto) {
        eventBus.publish(abilityCreation.create(abilityDto))
    }
}
