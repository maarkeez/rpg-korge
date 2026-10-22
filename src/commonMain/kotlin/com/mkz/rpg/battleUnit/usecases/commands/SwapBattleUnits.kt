package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestSwapOccupants
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.shared.domain.EventBus

/**
 * Walks both battle units onto each other's tile at once, outside a battle (e.g.: the leader of the party and the party
 * member on its next tile), spending no turn action. Nothing happens when any of them is not on the battlefield.
 */
class SwapBattleUnits(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchPosition: SearchPosition,
) {
    operator fun invoke(
        battleUnitId: String,
        otherBattleUnitId: String,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        val storedOtherBattleUnit = battleUnitRepository.searchById(otherBattleUnitId) ?: return
        val position = searchPosition(battleUnitId) ?: return
        val otherPosition = searchPosition(otherBattleUnitId) ?: return
        val (events, battleUnit) =
            storedBattleUnit
                .swap(
                    fromRow = position.row,
                    fromColumn = position.column,
                    toRow = otherPosition.row,
                    toColumn = otherPosition.column,
                    swappedWithBattleUnitId = otherBattleUnitId,
                ).pullEvents()
        val (otherEvents, otherBattleUnit) =
            storedOtherBattleUnit
                .swap(
                    fromRow = otherPosition.row,
                    fromColumn = otherPosition.column,
                    toRow = position.row,
                    toColumn = position.column,
                    swappedWithBattleUnitId = battleUnitId,
                ).pullEvents()
        battleUnitRepository.update(battleUnit)
        battleUnitRepository.update(otherBattleUnit)
        eventBus.publish(events + otherEvents + RequestSwapOccupants(occupantId = battleUnitId, otherOccupantId = otherBattleUnitId))
    }
}
