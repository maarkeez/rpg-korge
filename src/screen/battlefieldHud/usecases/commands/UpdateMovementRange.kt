package screen.battlefieldHud.usecases.commands

import battlefield.adapters.presentation.*
import battleunit.adapters.presentation.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import shared.domain.*

class UpdateMovementRange(
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is DisplayMovementRange -> {
                if(battleUnitId != battlefieldHud.battleUnitId) return
                val position = battlefieldApi.searchPosition(battleUnitId)!!
                val (events, updatedBattlefieldHud) = battlefieldHud
                    .tilesWhereCanBeMoved(
                        tile = TileDto(position.row, position.column),
                        tilesWhereCanBeMoved = tilesWhereCanMove(battleUnitId)
                    )
                    .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
            is Idle,
            is DisplayAbilityCastRange,
            is DisplayAbilityCastPreview  -> return
        }
    }

    private fun tilesWhereCanMove(battleUnitId: String): Set<TileDto> {
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
