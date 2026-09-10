package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class RequestAbilityCreation(
    private val abilityRepository: AbilityRepository,
    private val searchEffectById: SearchEffectById,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityDto: Ability.Dto) {
        if (abilityRepository.searchById(abilityDto.id) != null) return
        val allEffectsExist = abilityDto.effects.all { effectId -> searchEffectById(effectId) != null }
        if (!allEffectsExist) throw AbilityEffectDoesNotExist()
        val (events, ability) = Ability.create(abilityDto).pullEvents()
        abilityRepository.create(ability)
        eventBus.publish(events)
    }
}
