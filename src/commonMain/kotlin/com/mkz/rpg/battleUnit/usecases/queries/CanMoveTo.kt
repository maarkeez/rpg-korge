package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition

class CanMoveTo(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
) {
    operator fun invoke(
        battleUnitId: String,
        moveToRow: Int,
        moveToColumn: Int,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        val currentPosition = searchPosition(battleUnitId) ?: return false
        val pathDistance =
            shortestPathDistance(
                fromRow = currentPosition.row,
                fromColumn = currentPosition.column,
                toRow = moveToRow,
                toColumn = moveToColumn,
            ) ?: return false
        return battleUnit.canMoveDistance(pathDistance)
    }

    private fun shortestPathDistance(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): Int? {
        val startPosition = Position(fromRow, fromColumn)
        val visitedPositions = mutableSetOf(startPosition)
        val positionsDistance = mutableMapOf(startPosition to 0)
        val queue = ArrayDeque<Position>()
        queue.addLast(startPosition)
        while (queue.isNotEmpty()) {
            val currentPosition = queue.removeFirst()
            val currentDistance = positionsDistance.getValue(currentPosition)
            if (currentPosition.row == toRow && currentPosition.column == toColumn) return currentDistance
            for (nextPosition in adjacentPositions(currentPosition)) {
                if (nextPosition !in visitedPositions && canBattlefieldTileBeOccupied(nextPosition.row, nextPosition.column)) {
                    visitedPositions += nextPosition
                    positionsDistance[nextPosition] = currentDistance + 1
                    queue.addLast(nextPosition)
                }
            }
        }
        return null
    }

    private fun adjacentPositions(position: Position): List<Position> =
        listOf(
            Position(position.row - 1, position.column),
            Position(position.row + 1, position.column),
            Position(position.row, position.column - 1),
            Position(position.row, position.column + 1),
        )

    private data class Position(
        val row: Int,
        val column: Int,
    )
}
