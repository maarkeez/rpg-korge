package com.mkz.rpg.screen.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
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
            is DisplayAbilityCastPreview -> selectTileWhenPreviewingAbilityCast(battlefieldHud, tile)
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
        if (!isPlayerBattleUnit) {
            val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        } else if (selectedCastGroup == null) {
            selectOutsideCastGroups(battlefieldHud, casterBattleUnit.playerId, tile)
        } else {
            val (events, updatedBattlefieldHud) = battlefieldHud.previewAbilityCast(castGroup = selectedCastGroup).pullEvents()
            battlefieldHudRepository.update(updatedBattlefieldHud)
            eventBus.publish(events)
        }
    }

    /** A tap on another valid cast group switches the preview. Any other tap, including the previewed group, does nothing. */
    private fun selectTileWhenPreviewingAbilityCast(
        battlefieldHud: DisplayAbilityCastPreview,
        tile: TileDto,
    ) {
        val otherCastGroup =
            battlefieldHud.castGroupsWhereCanCast.firstOrNull { castGroup -> castGroup.tiles.contains(tile) } ?: return
        if (otherCastGroup == battlefieldHud.castGroup) return
        val (events, updatedBattlefieldHud) = battlefieldHud.previewAbilityCast(newCastGroup = otherCastGroup).pullEvents()
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }

    /** An invalid tap never loses the unit selection: it switches to another ally or goes back to the movement range. */
    private fun selectOutsideCastGroups(
        battlefieldHud: DisplayAbilityCastRange,
        casterPlayerId: String,
        tile: TileDto,
    ) {
        val occupantId = searchOccupant(row = tile.row, column = tile.column)
        val otherAlly =
            occupantId
                ?.takeIf { it != battlefieldHud.battleUnitId }
                ?.takeIf { searchBattleUnitById(it)?.playerId == casterPlayerId }
        val updatedHud =
            if (otherAlly != null) {
                battlefieldHud
                    .idle()
                    .selectBattleUnit(
                        tile = tile,
                        battleUnitId = otherAlly,
                        tilesWhereCanBeMoved = movementService.tilesWhereCanMove(otherAlly),
                    )
            } else {
                battlefieldHud.deselectAbility()
            }
        val (events, updatedBattlefieldHud) = updatedHud.pullEvents()
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }
}
