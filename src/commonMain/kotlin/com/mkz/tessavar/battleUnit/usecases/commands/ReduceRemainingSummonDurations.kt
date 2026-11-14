package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.shared.domain.EventBus

class ReduceRemainingSummonDurations(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitIds: Set<String>,
    ) {
        battleUnitRepository
            .searchByPlayerId(playerId)
            .filter { battleUnit -> battleUnit.toDto().id in battleUnitIds }
            .forEach { battleUnit ->
                val position = searchPosition(occupantId = battleUnit.toDto().id) ?: return@forEach
                val (events, updatedBattleUnit) = battleUnit.reduceRemainingSummonDuration(row = position.row, column = position.column).pullEvents()
                if (events.isEmpty()) {
                    battleUnitRepository.update(updatedBattleUnit)
                } else {
                    battleUnitRepository.delete(battleUnitId = updatedBattleUnit.toDto().id)
                }
                eventBus.publish(events)
            }
    }
}
