package com.mkz.rpg.ability.usecases.services

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.rpg.ability.domain.AbilityError.AbilityTurnsDurationMissing
import com.mkz.rpg.ability.domain.AbilityError.AbilityTurnsDurationNotAllowed
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
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
        val effects = abilityDto.effectSpecs.map { effectSpec -> searchEffectById(effectSpec.effectId) ?: throw AbilityEffectDoesNotExist() }
        val deploysBattleUnit = effects.any { effect -> effect.outcome.type == DEPLOY_BATTLE_UNIT }
        if (deploysBattleUnit && abilityDto.turnsDuration == null) throw AbilityTurnsDurationMissing()
        if (!deploysBattleUnit && abilityDto.turnsDuration != null) throw AbilityTurnsDurationNotAllowed()
        val (events, ability) = Ability.create(abilityDto).pullEvents()
        abilityRepository.create(ability)
        return events
    }
}
