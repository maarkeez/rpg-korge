package screen.battlefieldHud.usecases.commands

import battlefield.adapters.presentation.*
import battleunit.adapters.presentation.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import shared.domain.*

class ProcessTileSelected(
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(row: Int, column: Int) {
        val tile = TileDto(row = row, column = column)
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is BattlefieldHud.Idle -> selectTileWhenIdle(battlefieldHud, tile)
            is BattlefieldHud.DisplayMovementRange -> {
                // Do nothing for now
            }
        }
    }

    private fun selectTileWhenIdle(
        battlefieldHud: BattlefieldHud.Idle,
        tile: TileDto
    ) {
        val battleUnitId = battlefieldApi.searchOccupant(row = tile.row, column = tile.column) ?: return
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
        val (events, updatedBattlefieldHud) = battlefieldHud.selectBattleUnit(
            tile = tile,
            battleUnitId = battleUnitId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved
        ).pullEvents()
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }
}
