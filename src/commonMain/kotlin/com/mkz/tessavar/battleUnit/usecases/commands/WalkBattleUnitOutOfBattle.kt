package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.battlefield.usecases.queries.SearchShortestPath
import com.mkz.tessavar.shared.domain.EventBus

class WalkBattleUnitOutOfBattle(
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
        if (path.isEmpty()) return
        val (events, battleUnit) =
            storedBattleUnit
                .walkOutOfBattle(
                    fromRow = currentPosition.row,
                    fromColumn = currentPosition.column,
                    path = path,
                ).pullEvents()
        battleUnitRepository.update(battleUnit)
        eventBus.publish(events)
    }
}
