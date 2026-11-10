package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionError.SelectionNotFound
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.rpg.shared.domain.EventBus

class ProcessTileSelected(
    private val searchOccupant: SearchOccupant,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchBattle: SearchBattle,
    private val selectionRepository: SelectionRepository,
    private val eventBus: EventBus,
    private val walkingRangeService: WalkingRangeService,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ) {
        val tile = TileDto(row = row, column = column)
        val selection = selectionRepository.search() ?: throw SelectionNotFound()
        val isBattleInProgress = searchBattle()?.isFinished == false
        when (selection) {
            is NothingSelected -> if (isBattleInProgress) selectTileWhenNothingSelected(selection, tile) else selectTileWhileExploring(selection, tile)
            is BattleUnitSelected -> if (isBattleInProgress) selectTileWhenBattleUnitSelected(selection, tile) else selectTileWhileExploring(selection, tile)
            is AbilitySelected -> selectTileWhenAbilitySelected(selection, tile)
            is AbilityInfoDisplayed -> {
                val (events, updatedSelection) = selection.hideAbilityInfo().pullEvents()
                selectionRepository.update(updatedSelection)
                eventBus.publish(events)
            }
            is CastPreviewed -> Unit
        }
    }

    private fun selectTileWhileExploring(
        selection: Selection,
        tile: TileDto,
    ) {
        val selectedBattleUnitId = (selection as? BattleUnitSelected)?.battleUnitId
        val battleUnitId = humanOccupant(tile)?.takeIf { it != selectedBattleUnitId }
        val nothingSelected =
            when (selection) {
                is NothingSelected -> if (battleUnitId == null) selection.deselect() else selection
                is BattleUnitSelected -> selection.deselect()
                else -> return
            }
        val (events, updatedSelection) =
            if (battleUnitId == null) {
                nothingSelected.pullEvents()
            } else {
                nothingSelected.selectBattleUnit(tile = tile, battleUnitId = battleUnitId, walkingRange = emptySet()).pullEvents()
            }
        selectionRepository.update(updatedSelection)
        eventBus.publish(events)
    }

    private fun humanOccupant(tile: TileDto): String? {
        val battleUnitId = searchOccupant(row = tile.row, column = tile.column) ?: return null
        val battleUnit = searchBattleUnitById(battleUnitId) ?: return null
        return battleUnitId.takeIf { searchPlayerById(battleUnit.playerId)?.type == Player.Dto.PlayerTypeDto.HUMAN }
    }

    private fun occupantInBattle(tile: TileDto): String? {
        val battleUnitId = searchOccupant(row = tile.row, column = tile.column) ?: return null
        return battleUnitId.takeIf { it in searchBattle()?.battleUnitIds.orEmpty() }
    }

    private fun selectTileWhenBattleUnitSelected(
        selection: BattleUnitSelected,
        tile: TileDto,
    ) {
        val battleUnitId = occupantInBattle(tile)
        if (battleUnitId == null) {
            val battleUnit = searchBattleUnitById(selection.battleUnitId)!!
            val selectedTileInMovementRange = selection.walkingRange.contains(tile)
            val player = searchPlayerById(battleUnit.playerId)!!
            if (player.type != Player.Dto.PlayerTypeDto.HUMAN) {
                val (events, updatedSelection) = selection.deselect().pullEvents()
                selectionRepository.update(updatedSelection)
                eventBus.publish(events)
            } else {
                if (selectedTileInMovementRange) {
                    eventBus.publish(
                        BattleUnitEvent.RequestWalkBattleUnitInBattle(
                            battleUnitId = battleUnit.id,
                            walkToRow = tile.row,
                            walkToColumn = tile.column,
                        ),
                    )
                } else {
                    val (events, updatedSelection) =
                        selection
                            .deselect()
                            .pullEvents()
                    selectionRepository.update(updatedSelection)
                    eventBus.publish(events)
                }
            }
        } else if (selection.battleUnitId == battleUnitId) {
            val (events, updatedSelection) = selection.deselect().pullEvents()
            selectionRepository.update(updatedSelection)
            eventBus.publish(events)
        } else {
            val walkingRange = walkingRangeService.walkingRange(battleUnitId)
            val (events, updatedSelection) =
                selection
                    .deselect()
                    .selectBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        walkingRange = walkingRange,
                    ).pullEvents()
            selectionRepository.update(updatedSelection)
            eventBus.publish(events)
        }
    }

    private fun selectTileWhenNothingSelected(
        selection: NothingSelected,
        tile: TileDto,
    ) {
        val battleUnitId = occupantInBattle(tile)
        if (battleUnitId == null) {
            val (events, updatedSelection) = selection.deselect().pullEvents()
            selectionRepository.update(updatedSelection)
            eventBus.publish(events)
        } else {
            val walkingRange = walkingRangeService.walkingRange(battleUnitId)
            val (events, updatedSelection) =
                selection
                    .selectBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        walkingRange = walkingRange,
                    ).pullEvents()
            selectionRepository.update(updatedSelection)
            eventBus.publish(events)
        }
    }

    private fun selectTileWhenAbilitySelected(
        selection: AbilitySelected,
        tile: TileDto,
    ) {
        val casterBattleUnit = searchBattleUnitById(selection.battleUnitId)!!
        val currentPlayerId = searchBattle()?.currentPlayerId ?: return

        val selectedCastGroup = selection.castGroupsWhereCanCast.firstOrNull { castGroup -> castGroup.tiles.contains(tile) }
        val isPlayerBattleUnit = currentPlayerId == casterBattleUnit.playerId
        if (selectedCastGroup == null || !isPlayerBattleUnit) {
            val (events, updatedSelection) = selection.deselect().pullEvents()
            selectionRepository.update(updatedSelection)
            eventBus.publish(events)
        } else {
            val targetBattleUnitId = searchOccupant(tile.row, tile.column)
            val isSameUnit = casterBattleUnit.id == targetBattleUnitId
            val noTargetBattleUnit = targetBattleUnitId == null
            if (isSameUnit || noTargetBattleUnit) {
                val (events, updatedSelection) = selection.previewCastOnCasterOrVacantTile(castGroup = selectedCastGroup).pullEvents()
                selectionRepository.update(updatedSelection)
                eventBus.publish(events)
            } else {
                val recipientBattleUnitIds =
                    selectedCastGroup.tiles
                        .sortedWith(compareBy(TileDto::row, TileDto::column))
                        .mapNotNull { castTile -> searchOccupant(castTile.row, castTile.column) }
                        .filterNot { battleUnitId -> battleUnitId == casterBattleUnit.id }
                val (events, updatedSelection) =
                    selection
                        .previewCastOnBattleUnits(castGroup = selectedCastGroup, recipientBattleUnitIds = recipientBattleUnitIds)
                        .pullEvents()
                selectionRepository.update(updatedSelection)
                eventBus.publish(events)
            }
        }
    }
}
