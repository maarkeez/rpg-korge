package screen.battlefieldHud.usecases.commands

import battle.usecases.queries.SearchBattle
import battleUnit.usecases.commands.MoveBattleUnit
import battleUnit.usecases.queries.SearchBattleUnitById
import battlefield.usecases.queries.SearchOccupant
import player.domain.Player
import player.usecases.queries.SearchPlayerById
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.domain.BattlefieldHudRepository
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.EventBus

class ProcessTileSelected(
    private val searchOccupant: SearchOccupant,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val moveBattleUnit: MoveBattleUnit,
    private val searchPlayerById: SearchPlayerById,
    private val searchBattle: SearchBattle,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
    private val movementService: MovementService,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ) {
        val tile = TileDto(row = row, column = column)
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
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
        val battleUnitId = searchOccupant(row = tile.row, column = tile.column)
        if (battleUnitId == null) {
            val battleUnit = searchBattleUnitById(battlefieldHud.battleUnitId)!!
            val selectedTileInMovementRange = battlefieldHud.tilesWhereCanBeMoved.contains(tile)
            val player = searchPlayerById(battleUnit.playerId)!!
            if (player.type != Player.Dto.PlayerTypeDto.HUMAN) {
                val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            } else {
                if (selectedTileInMovementRange) {
                    moveBattleUnit(battleUnitId = battleUnit.id, moveToRow = tile.row, moveToColumn = tile.column)
                } else {
                    val (events, updatedBattlefieldHud) =
                        battlefieldHud
                            .idle()
                            .pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                }
            }
        } else if (battlefieldHud.battleUnitId == battleUnitId) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else {
            val tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId)
            val (events, updatedBattlefieldHud) =
                battlefieldHud
                    .idle()
                    .selectBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                    ).pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }
    }

    private fun selectTileWhenIdle(
        battlefieldHud: Idle,
        tile: TileDto,
    ) {
        val battleUnitId = searchOccupant(row = tile.row, column = tile.column)
        if (battleUnitId == null) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else {
            val tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId)
            val (events, updatedBattlefieldHud) =
                battlefieldHud
                    .selectBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                    ).pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }
    }

    private fun selectTileWhenDisplayingAbilityCastRange(
        battlefieldHud: DisplayAbilityCastRange,
        tile: TileDto,
    ) {
        val casterBattleUnit = searchBattleUnitById(battlefieldHud.battleUnitId)!!
        val currentPlayerId = searchBattle()?.currentPlayerTurn ?: return

        val tileIsACastPosition = battlefieldHud.tilesWhereCanCast.contains(tile)
        val isPlayerBattleUnit = currentPlayerId == casterBattleUnit.playerId
        if (!tileIsACastPosition || !isPlayerBattleUnit) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else {
            val targetBattleUnitId = searchOccupant(tile.row, tile.column)
            val isSameUnit = casterBattleUnit.id == targetBattleUnitId
            val noTargetBattleUnit = targetBattleUnitId == null
            if (isSameUnit || noTargetBattleUnit) {
                val (events, updatedBattlefieldHud) = battlefieldHud.previewSelfAbilityCast(castTile = tile).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            } else {
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .previewEnemyAbilityCast(castTile = tile, enemyBattleUnitId = targetBattleUnitId)
                        .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
        }
    }
}
