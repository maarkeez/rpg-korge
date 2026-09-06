package screen.battlefieldHud.usecases.services

import battlefield.adapters.presentation.*
import battleunit.adapters.presentation.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto

class MovementService(
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
) {
    fun tilesWhereCanMove(battleUnitId: String): Set<TileDto> {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId)!!
        val tilesWhereCanBeMoved = battlefieldApi.searchTilesThatCanBeOccupied(
            battleUnitId = battleUnit.id,
            distance = battleUnit.remainingTurnActions.remainingSteps
        ).filter { tilePosition ->
            battleUnitApi.canMoveTo(
                battleUnitId = battleUnit.id,
                moveToRow = tilePosition.row,
                moveToColumn = tilePosition.column
            )
        }
            .map { tilePosition -> TileDto(row = tilePosition.row, column = tilePosition.column) }
            .toSet()
        return tilesWhereCanBeMoved
    }
}
