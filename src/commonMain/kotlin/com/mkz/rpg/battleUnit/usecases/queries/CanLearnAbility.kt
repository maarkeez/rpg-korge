package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

/** Whether the battle unit can learn the ability for good: it does not know it yet, and can know one more ability. */
class CanLearnAbility(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        return battleUnit.canLearnAbility(abilityId)
    }
}
