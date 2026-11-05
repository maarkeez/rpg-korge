package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services

import com.mkz.rpg.battleUnit.usecases.queries.CanWalkTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.usecases.queries.SearchVacantTiles
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto

class MovementService(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchVacantTiles: SearchVacantTiles,
    private val canWalkTo: CanWalkTo,
) {
    fun tilesWhereCanMove(battleUnitId: String): Set<TileDto> {
        val battleUnit = searchBattleUnitById(battleUnitId)!!
        val tilesWhereCanBeMoved =
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
        return tilesWhereCanBeMoved
    }
}
