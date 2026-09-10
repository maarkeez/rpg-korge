package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.shared.domain.EventBus

class MoveBattleUnit(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchPosition: SearchPosition,
    private val distanceService: DistanceService,
) {
    operator fun invoke(
        battleUnitId: String,
        moveToRow: Int,
        moveToColumn: Int,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        val currentPosition = searchPosition(battleUnitId) ?: return
        val distance =
            distanceService.manhattanDistance(
                fromRow = currentPosition.row,
                fromColumn = currentPosition.column,
                toRow = moveToRow,
                toColumn = moveToColumn,
            )
        val (events, battleUnit) =
            storedBattleUnit
                .move(
                    distance = distance,
                    fromRow = currentPosition.row,
                    fromColumn = currentPosition.column,
                    toRow = moveToRow,
                    toColumn = moveToColumn,
                ).pullEvents()
        battleUnitRepository.create(battleUnit)
        eventBus.publish(events)
    }
}
