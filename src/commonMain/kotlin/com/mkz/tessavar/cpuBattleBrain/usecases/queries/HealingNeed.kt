package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById

class HealingNeed(
    private val searchBattleUnitById: SearchBattleUnitById,
) {
    operator fun invoke(battleUnitId: String): Double {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return 0.0
        if (battleUnit.maximumHealth <= 0) return 1.0
        val healingNeed = 1.0 - battleUnit.remainingHealth.toDouble() / battleUnit.maximumHealth.toDouble()
        return healingNeed.coerceIn(0.0, 1.0)
    }
}
