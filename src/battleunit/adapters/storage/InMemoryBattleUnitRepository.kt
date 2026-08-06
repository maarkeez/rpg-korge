package battleunit.adapters.storage

import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitRepository
import kotlin.collections.set

class InMemoryBattleUnitRepository: BattleUnitRepository {
    private val battleUnits = mutableMapOf<String, BattleUnit>()
    override fun create(battleUnit: BattleUnit) {
        battleUnits[battleUnit.toDto().id] = battleUnit
    }

    override fun searchById(id: String) = battleUnits[id]
}
