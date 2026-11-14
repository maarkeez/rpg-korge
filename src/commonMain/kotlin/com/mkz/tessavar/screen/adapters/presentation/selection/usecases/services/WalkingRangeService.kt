package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.services

import com.mkz.tessavar.battleUnit.usecases.queries.CanWalkTo
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.battlefield.usecases.queries.SearchVacantTiles
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto

class WalkingRangeService(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchVacantTiles: SearchVacantTiles,
    private val canWalkTo: CanWalkTo,
) {
    fun walkingRange(battleUnitId: String): Set<TileDto> {
        val battleUnit = searchBattleUnitById(battleUnitId)!!
        val walkingRange =
            searchVacantTiles(
                occupantId = battleUnit.id,
                tilesAround = battleUnit.remainingActions.remainingSteps,
            ).filter { tilePosition ->
                canWalkTo(
                    battleUnitId = battleUnit.id,
                    walkToRow = tilePosition.row,
                    walkToColumn = tilePosition.column,
                )
            }.map { tilePosition -> TileDto(row = tilePosition.row, column = tilePosition.column) }
                .toSet()
        return walkingRange
    }
}
