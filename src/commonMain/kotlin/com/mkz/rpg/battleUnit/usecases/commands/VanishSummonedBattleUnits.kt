package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once the battle ends, every summon left vanishes, whatever turns it has left, as the summons never explore. They are
 * deleted, so nothing counts them anymore.
 */
class VanishSummonedBattleUnits(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        battleUnitRepository.searchAll().forEach { battleUnit ->
            val position = searchPosition(occupantId = battleUnit.toDto().id) ?: return@forEach
            val (events, vanishedBattleUnit) = battleUnit.vanish(row = position.row, column = position.column).pullEvents()
            if (events.isEmpty()) return@forEach
            battleUnitRepository.delete(battleUnitId = vanishedBattleUnit.toDto().id)
            eventBus.publish(events)
        }
    }
}
