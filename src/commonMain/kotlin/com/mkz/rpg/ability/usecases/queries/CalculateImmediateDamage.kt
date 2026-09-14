package com.mkz.rpg.ability.usecases.queries

import com.mkz.rpg.ability.domain.AbilityRepository
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

class CalculateImmediateDamage(
    private val abilityRepository: AbilityRepository,
    private val searchEffectById: SearchEffectById,
) {
    operator fun invoke(id: String): Int {
        val ability = abilityRepository.searchById(id)?.toDto() ?: return 0
        val effects = ability.effects.map { effectId -> searchEffectById(effectId)!! }
        return effects
            .filter { it.application.type == "IMMEDIATELY" }
            .filter { effect -> effect.outcome.type == DECREASE_HEALTH }
            .sumOf { effect -> effect.outcome.decreaseHealth!!.damage }
    }
}
