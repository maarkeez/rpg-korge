package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition

class CanMoveTo(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val distanceService: DistanceService,
) {
    operator fun invoke(
        battleUnitId: String,
        moveToRow: Int,
        moveToColumn: Int,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        val currentPosition = searchPosition(battleUnitId) ?: return false
        val distance =
            distanceService.manhattanDistance(
                fromRow = currentPosition.row,
                fromColumn = currentPosition.column,
                toRow = moveToRow,
                toColumn = moveToColumn,
            )
        return battleUnit.canMoveDistance(distance)
    }
}
