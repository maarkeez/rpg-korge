package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class CanCastAbility(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchAbilityById: SearchAbilityById,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        val ability = searchAbilityById(abilityId) ?: return false
        // TODO: Consider ability cost
        return battleUnit.canCastAbility(ability)
    }
}
