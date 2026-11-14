package com.mkz.tessavar.ability.usecases.services

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.tessavar.ability.domain.AbilityError.AbilitySummonDurationMissing
import com.mkz.tessavar.ability.domain.AbilityError.AbilitySummonDurationNotAllowed
import com.mkz.tessavar.ability.domain.AbilityEvent
import com.mkz.tessavar.ability.domain.AbilityRepository
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.SUMMON
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById

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
