package com.mkz.rpg.ability.usecases.services

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.rpg.ability.domain.AbilityError.AbilitySummonDurationMissing
import com.mkz.rpg.ability.domain.AbilityError.AbilitySummonDurationNotAllowed
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.SUMMON
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

/**
 * Creates an ability, unless it already exists, once all its effects exist. An ability deploying a battle unit must say
 * how many turns it lasts, and the others must not. Returns the events to publish.
 */
class AbilityCreation(
    private val abilityRepository: AbilityRepository,
    private val searchEffectById: SearchEffectById,
) {
    fun create(abilityDto: Ability.Dto): Set<AbilityEvent> {
        if (abilityRepository.searchById(abilityDto.id) != null) return emptySet()
        val effects = abilityDto.abilityEffects.map { abilityEffect -> searchEffectById(abilityEffect.effectId) ?: throw AbilityEffectDoesNotExist() }
        val deploysBattleUnit = effects.any { effect -> effect.outcome.type == SUMMON }
        if (deploysBattleUnit && abilityDto.summonDuration == null) throw AbilitySummonDurationMissing()
        if (!deploysBattleUnit && abilityDto.summonDuration != null) throw AbilitySummonDurationNotAllowed()
        val (events, ability) = Ability.create(abilityDto).pullEvents()
        abilityRepository.create(ability)
        return events
    }
}
