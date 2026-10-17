package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.EventBus

class ProcessTileSelected(
    private val searchOccupant: SearchOccupant,
    private val searchBattleUnitById: SearchBattleUnitById,
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
        val isBattleInProgress = searchBattle()?.isFinished == false
        when (battlefieldHud) {
            is Idle -> if (isBattleInProgress) selectTileWhenIdle(battlefieldHud, tile) else selectTileWhileExploring(battlefieldHud, tile)
            is DisplayMovementRange -> if (isBattleInProgress) selectTileWhenDisplayingMovement(battlefieldHud, tile) else selectTileWhileExploring(battlefieldHud, tile)
            is DisplayAbilityCastRange -> selectTileWhenDisplayingAbilityCastRange(battlefieldHud, tile)
            is DisplayAbilityCastPreview -> {
                // Do nothing
            }
        }
    }

    // Out of the battles (e.g.: while exploring), only the battle units of the human player can be selected, to use their
    // inventory. They can not move by selecting a tile, so they have no movement range. Anything else goes idle.
    private fun selectTileWhileExploring(
        battlefieldHud: BattlefieldHud,
        tile: TileDto,
    ) {
        val selectedBattleUnitId = (battlefieldHud as? DisplayMovementRange)?.battleUnitId
        val battleUnitId = humanOccupant(tile)?.takeIf { it != selectedBattleUnitId }
        val idleBattlefieldHud =
            when (battlefieldHud) {
                is Idle -> if (battleUnitId == null) battlefieldHud.idle() else battlefieldHud
                is DisplayMovementRange -> battlefieldHud.idle()
                else -> return
            }
        val (events, updatedBattlefieldHud) =
            if (battleUnitId == null) {
                idleBattlefieldHud.pullEvents()
            } else {
                idleBattlefieldHud.selectBattleUnit(tile = tile, battleUnitId = battleUnitId, tilesWhereCanBeMoved = emptySet()).pullEvents()
            }
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }

    private fun humanOccupant(tile: TileDto): String? {
        val battleUnitId = searchOccupant(row = tile.row, column = tile.column) ?: return null
        val battleUnit = searchBattleUnitById(battleUnitId) ?: return null
        return battleUnitId.takeIf { searchPlayerById(battleUnit.playerId)?.type == Player.Dto.PlayerTypeDto.HUMAN }
    }

    // The battle unit on the tile, when it is in the battle. Battle units out of it can not be selected, as an empty tile.
    private fun occupantInBattle(tile: TileDto): String? {
        val battleUnitId = searchOccupant(row = tile.row, column = tile.column) ?: return null
        return battleUnitId.takeIf { it in searchBattle()?.battleUnitIds.orEmpty() }
    }

    private fun selectTileWhenDisplayingMovement(
        battlefieldHud: DisplayMovementRange,
        tile: TileDto,
    ) {
        val battleUnitId = occupantInBattle(tile)
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
                    eventBus.publish(
                        BattleUnitEvent.RequestMoveBattleUnit(
                            battleUnitId = battleUnit.id,
                            moveToRow = tile.row,
                            moveToColumn = tile.column,
                        ),
                    )
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
        val battleUnitId = occupantInBattle(tile)
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
                // Every enemy of the cast group receives the cast, not only the selected one.
                val enemyBattleUnitIds =
                    selectedCastGroup.tiles
                        .sortedWith(compareBy(TileDto::row, TileDto::column))
                        .mapNotNull { castTile -> searchOccupant(castTile.row, castTile.column) }
                        .filterNot { battleUnitId -> battleUnitId == casterBattleUnit.id }
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .previewEnemyAbilityCast(castGroup = selectedCastGroup, enemyBattleUnitIds = enemyBattleUnitIds)
                        .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
        }
    }
}
