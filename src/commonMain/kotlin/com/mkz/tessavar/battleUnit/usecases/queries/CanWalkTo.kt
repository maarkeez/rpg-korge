package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.battlefield.usecases.queries.SearchShortestPath

class CanWalkTo(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val searchShortestPath: SearchShortestPath,
) {
    operator fun invoke(
        battleUnitId: String,
        walkToRow: Int,
        walkToColumn: Int,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        val currentPosition = searchPosition(battleUnitId) ?: return false
        val path =
            searchShortestPath(
                fromRow = currentPosition.row,
                fromColumn = currentPosition.column,
                toRow = walkToRow,
                toColumn = walkToColumn,
            ) ?: return false
        return battleUnit.canWalkDistance(path.size)
    }
}
