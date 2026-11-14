package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository

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
        return battleUnit.canCastAbility(ability)
    }
}
