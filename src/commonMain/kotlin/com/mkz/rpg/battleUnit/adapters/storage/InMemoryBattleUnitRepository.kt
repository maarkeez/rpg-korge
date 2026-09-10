package com.mkz.rpg.battleUnit.adapters.storage

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class InMemoryBattleUnitRepository : BattleUnitRepository {
    private val battleUnits = mutableMapOf<String, BattleUnit>()

    override fun create(battleUnit: BattleUnit) {
        battleUnits[battleUnit.toDto().id] = battleUnit
    }

    override fun update(battleUnit: BattleUnit) {
        battleUnits[battleUnit.toDto().id] = battleUnit
    }

    override fun searchById(id: String) = battleUnits[id]

    override fun searchByPlayerId(playerId: String) =
        battleUnits.values
            .filter { it.toDto().playerId == playerId }

    override fun searchAll(): List<BattleUnit> = battleUnits.values.toList()
}
