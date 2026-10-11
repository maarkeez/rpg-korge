package com.mkz.rpg.ability.usecases.services

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

/** Creates an ability, unless it already exists, once all its effects exist. Returns the events to publish. */
class AbilityCreation(
    private val abilityRepository: AbilityRepository,
    private val searchEffectById: SearchEffectById,
) {
    fun create(abilityDto: Ability.Dto): Set<AbilityEvent> {
        if (abilityRepository.searchById(abilityDto.id) != null) return emptySet()
        val allEffectsExist = abilityDto.effectSpecs.all { effectSpec -> searchEffectById(effectSpec.effectId) != null }
        if (!allEffectsExist) throw AbilityEffectDoesNotExist()
        val (events, ability) = Ability.create(abilityDto).pullEvents()
        abilityRepository.create(ability)
        return events
    }
}
