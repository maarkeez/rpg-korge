package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestSwapOccupants
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.shared.domain.EventBus

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
