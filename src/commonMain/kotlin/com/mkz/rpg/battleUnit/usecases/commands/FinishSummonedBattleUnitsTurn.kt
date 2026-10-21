package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once a player finishes its turn, its summons in the battle have one turn fewer. Those with no turn left vanish: they
 * are deleted, so nothing counts them anymore. Battle units: those in the battle.
 */
class FinishSummonedBattleUnitsTurn(
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
                val (events, updatedBattleUnit) = battleUnit.finishTurn(row = position.row, column = position.column).pullEvents()
                if (events.isEmpty()) {
                    battleUnitRepository.update(updatedBattleUnit)
                } else {
                    battleUnitRepository.delete(battleUnitId = updatedBattleUnit.toDto().id)
                }
                eventBus.publish(events)
            }
    }
}
