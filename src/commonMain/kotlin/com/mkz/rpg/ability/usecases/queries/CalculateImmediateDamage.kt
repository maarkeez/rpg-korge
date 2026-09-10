package com.mkz.rpg.ability.usecases.queries

import com.mkz.rpg.ability.domain.AbilityRepository
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
            .sumOf { effect -> effect.power }
    }
}
