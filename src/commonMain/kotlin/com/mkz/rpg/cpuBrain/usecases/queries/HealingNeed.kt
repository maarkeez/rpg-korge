package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

class HealingNeed(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchUnitById: SearchUnitById,
) {
    operator fun invoke(battleUnitId: String): Double {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return 0.0
        val unit = searchUnitById(id = battleUnit.unitId) ?: return 0.0
        if (unit.healthPoints <= 0) return 1.0
        val healingNeed = 1.0 - battleUnit.remainingHealthPoints.toDouble() / unit.healthPoints.toDouble()
        return healingNeed.coerceIn(0.0, 1.0)
    }
}
