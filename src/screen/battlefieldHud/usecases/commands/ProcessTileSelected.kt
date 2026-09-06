package screen.battlefieldHud.usecases.commands

import ability.adapters.presentation.AbilityApi
import battle.adapters.presentation.BattleApi
import battlefield.adapters.presentation.*
import battleunit.adapters.presentation.*
import player.adapters.presentation.PlayerApi
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.*
import unit.adapters.presentation.UnitApi

class ProcessTileSelected(
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val battleApi: BattleApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
    private val movementService: MovementService,
) {
    operator fun invoke(row: Int, column: Int) {
        val tile = TileDto(row = row, column = column)
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is Idle -> selectTileWhenIdle(battlefieldHud, tile)
            is DisplayMovementRange -> selectTileWhenDisplayingMovement(battlefieldHud, tile)
            is DisplayAbilityCastRange -> selectTileWhenDisplayingAbilityCastRange(battlefieldHud, tile)
            is DisplayAbilityCastPreview -> {
                // Do nothing
            }
        }
    }

    private fun selectTileWhenDisplayingMovement(
        battlefieldHud: DisplayMovementRange,
        tile: TileDto,
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
                }else{
                    val (events, updatedBattlefieldHud) = battlefieldHud
                        .idle()
                        .pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                }
            }
        }else if(battlefieldHud.battleUnitId == battleUnitId){
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else {
            val tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId)
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
        battlefieldHud: Idle,
        tile: TileDto
    ) {
        val battleUnitId = battlefieldApi.searchOccupant(row = tile.row, column = tile.column)
        if(battleUnitId == null) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }else{
            val tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId)
            val (events, updatedBattlefieldHud) = battlefieldHud.selectBattleUnit(
                tile = tile,
                battleUnitId = battleUnitId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved
            ).pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }
    }

    private fun selectTileWhenDisplayingAbilityCastRange(
        battlefieldHud: DisplayAbilityCastRange,
        tile: TileDto
    ) {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(battlefieldHud.battleUnitId)!!
        val currentPlayerId = battleApi.searchBattle()?.currentPlayerTurn ?: return

        val tileIsACastPosition = battlefieldHud.tilesWhereCanCast.contains(tile)
        val isPlayerBattleUnit = currentPlayerId == casterBattleUnit.playerId
        if (!tileIsACastPosition || !isPlayerBattleUnit) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)

        }else{
            val targetBattleUnitId = battlefieldApi.searchOccupant(tile.row, tile.column)
            val isSameUnit = casterBattleUnit.id == targetBattleUnitId
            val noTargetBattleUnit = targetBattleUnitId == null
            if(isSameUnit || noTargetBattleUnit){
                val (events, updatedBattlefieldHud) = battlefieldHud.previewSelfAbilityCast(castTile = tile).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }else{
                val (events, updatedBattlefieldHud) = battlefieldHud
                    .previewEnemyAbilityCast(castTile = tile, enemyBattleUnitId= targetBattleUnitId)
                    .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
        }
    }
}
