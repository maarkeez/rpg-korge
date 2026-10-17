package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.shared.domain.EventBus

/** Walks the battle unit along the shortest path to the tile outside a battle (e.g.: while exploring), spending no turn action. */
class WalkBattleUnit(
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
                .walk(
                    fromRow = currentPosition.row,
                    fromColumn = currentPosition.column,
                    path = path,
                ).pullEvents()
        battleUnitRepository.update(battleUnit)
        eventBus.publish(events)
    }
}
