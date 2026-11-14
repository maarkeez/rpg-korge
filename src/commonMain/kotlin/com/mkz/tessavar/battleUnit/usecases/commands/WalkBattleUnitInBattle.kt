package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.battlefield.usecases.queries.SearchShortestPath
import com.mkz.tessavar.shared.domain.EventBus

class WalkBattleUnitInBattle(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchPosition: SearchPosition,
    private val searchShortestPath: SearchShortestPath,
) {
    operator fun invoke(
        battleUnitId: String,
        walkToRow: Int,
        walkToColumn: Int,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        val currentPosition = searchPosition(battleUnitId) ?: return
        val path =
            searchShortestPath(
                fromRow = currentPosition.row,
                fromColumn = currentPosition.column,
                toRow = walkToRow,
                toColumn = walkToColumn,
            ) ?: return
        val (events, battleUnit) =
            storedBattleUnit
                .walkInBattle(
                    fromRow = currentPosition.row,
                    fromColumn = currentPosition.column,
                    path = path,
                ).pullEvents()
        battleUnitRepository.create(battleUnit)
        eventBus.publish(events)
    }
}
