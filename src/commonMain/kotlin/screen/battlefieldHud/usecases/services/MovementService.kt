package screen.battlefieldHud.usecases.services

import battleUnit.usecases.queries.CanMoveTo
import battleUnit.usecases.queries.SearchBattleUnitById
import battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto

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
