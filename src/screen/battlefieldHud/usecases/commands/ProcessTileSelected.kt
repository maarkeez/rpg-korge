package screen.battlefieldHud.usecases.commands

import battlefield.adapters.presentation.*
import battleunit.adapters.presentation.*
import player.adapters.presentation.PlayerApi
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import shared.domain.*

class ProcessTileSelected(
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(row: Int, column: Int) {
        val tile = TileDto(row = row, column = column)
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is BattlefieldHud.Idle -> selectTileWhenIdle(battlefieldHud, tile)
            is BattlefieldHud.DisplayMovementRange -> selectTileWhenDisplayingMovement(tile, battlefieldHud)
            is BattlefieldHud.DisplayAbilityCastRange -> {

            }
        }
    }

    private fun selectTileWhenDisplayingMovement(
        tile: TileDto,
        battlefieldHud: BattlefieldHud.DisplayMovementRange
    ) {
        val battleUnitId = battlefieldApi.searchOccupant(row = tile.row, column = tile.column)
        if(battleUnitId == null) {
            val battleUnit = battleUnitApi.searchBattleUnitById(battlefieldHud.battleUnitId)!!
            val selectedTileInMovementRange = battlefieldHud.tilesWhereCanBeMoved.contains(tile)
            val player = playerApi.searchPlayerById(battleUnit.playerId)!!
            if(player.type != "HUMAN") {
                val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }else{
                if(selectedTileInMovementRange) {
                    battleUnitApi.moveBattleUnit(battleUnitId = battleUnit.id, moveToRow = tile.row, moveToColumn = tile.column)
                }
            }
        }else if(battlefieldHud.battleUnitId == battleUnitId){
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else {
            val tilesWhereCanBeMoved = tilesWhereCanMove(battleUnitId)
            val (events, updatedBattlefieldHud) = battlefieldHud
                .idle()
                .selectBattleUnit(
                    tile = tile,
                    battleUnitId = battleUnitId,
                    tilesWhereCanBeMoved = tilesWhereCanBeMoved
                )
                .pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }
    }

    private fun selectTileWhenIdle(
        battlefieldHud: BattlefieldHud.Idle,
        tile: TileDto
    ) {
        val battleUnitId = battlefieldApi.searchOccupant(row = tile.row, column = tile.column)
        if(battleUnitId == null) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }else{
            val tilesWhereCanBeMoved = tilesWhereCanMove(battleUnitId)
            val (events, updatedBattlefieldHud) = battlefieldHud.selectBattleUnit(
                tile = tile,
                battleUnitId = battleUnitId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved
            ).pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
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
