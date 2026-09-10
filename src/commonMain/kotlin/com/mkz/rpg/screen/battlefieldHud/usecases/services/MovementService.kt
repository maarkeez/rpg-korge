package com.mkz.rpg.screen.battlefieldHud.usecases.services

import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto

class MovementService(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchTilesThatCanBeOccupied: SearchTilesThatCanBeOccupied,
    private val canMoveTo: CanMoveTo,
) {
    fun tilesWhereCanMove(battleUnitId: String): Set<TileDto> {
        val battleUnit = searchBattleUnitById(battleUnitId)!!
        val tilesWhereCanBeMoved =
            searchTilesThatCanBeOccupied(
                battleUnitId = battleUnit.id,
                distance = battleUnit.remainingTurnActions.remainingSteps,
            ).filter { tilePosition ->
                canMoveTo(
                    battleUnitId = battleUnit.id,
                    moveToRow = tilePosition.row,
                    moveToColumn = tilePosition.column,
                )
            }.map { tilePosition -> TileDto(row = tilePosition.row, column = tilePosition.column) }
                .toSet()
        return tilesWhereCanBeMoved
    }
}
