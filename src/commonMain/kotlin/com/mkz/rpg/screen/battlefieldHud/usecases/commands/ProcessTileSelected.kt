package com.mkz.rpg.screen.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.EventBus

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

        val selectedCastGroup = battlefieldHud.castGroupsWhereCanCast.firstOrNull { castGroup -> castGroup.tiles.contains(tile) }
        val isPlayerBattleUnit = currentPlayerId == casterBattleUnit.playerId
        if (selectedCastGroup == null || !isPlayerBattleUnit) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else {
            val targetBattleUnitId = searchOccupant(tile.row, tile.column)
            val isSameUnit = casterBattleUnit.id == targetBattleUnitId
            val noTargetBattleUnit = targetBattleUnitId == null
            if (isSameUnit || noTargetBattleUnit) {
                val (events, updatedBattlefieldHud) = battlefieldHud.previewSelfAbilityCast(castGroup = selectedCastGroup).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            } else {
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .previewEnemyAbilityCast(castGroup = selectedCastGroup, enemyBattleUnitId = targetBattleUnitId)
                        .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
        }
    }
}
