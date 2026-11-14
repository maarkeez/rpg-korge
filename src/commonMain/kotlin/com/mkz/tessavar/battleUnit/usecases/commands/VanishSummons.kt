package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.shared.domain.EventBus

class VanishSummons(
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
