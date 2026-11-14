package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.battle.usecases.queries.SearchBattle
import com.mkz.tessavar.battleUnit.usecases.queries.CanCastAbility
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanCast
import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError.InvalidSelectionState
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError.SelectionNotFound
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionRepository
import com.mkz.tessavar.shared.domain.EventBus

class ProcessAbilitySelected(
    private val canCastAbility: CanCastAbility,
    private val whereCanCast: WhereCanCast,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchBattle: SearchBattle,
    private val selectionRepository: SelectionRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityId: String) {
        val selection = selectionRepository.search() ?: throw SelectionNotFound()
        val (events, updatedSelection) =
            when (selection) {
                is BattleUnitSelected ->
                    if (canCast(selection.battleUnitId, abilityId)) {
                        selection.selectAbility(abilityId, castGroupsWhereCanCast(selection.battleUnitId, abilityId)).pullEvents()
                    } else {
                        selection.displayAbilityInfo(abilityId).pullEvents()
                    }

                is AbilitySelected ->
                    if (abilityId == selection.abilityId) {
                        selection.deselectAbility().pullEvents()
                    } else if (canCast(selection.battleUnitId, abilityId)) {
                        selection.selectAbility(abilityId, castGroupsWhereCanCast(selection.battleUnitId, abilityId)).pullEvents()
                    } else {
                        selection.displayAbilityInfo(abilityId).pullEvents()
                    }

                is AbilityInfoDisplayed ->
                    if (abilityId == selection.abilityId) {
                        selection.hideAbilityInfo().pullEvents()
                    } else if (canCast(selection.battleUnitId, abilityId)) {
                        selection.selectAbility(abilityId, castGroupsWhereCanCast(selection.battleUnitId, abilityId)).pullEvents()
                    } else {
                        selection.displayAbilityInfo(abilityId).pullEvents()
                    }

                is NothingSelected,
                is CastPreviewed,
                -> throw InvalidSelectionState()
            }
        selectionRepository.update(updatedSelection)
        eventBus.publish(events)
    }

    private fun canCast(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val battleUnit = searchBattleUnitById(battleUnitId) ?: return false
        val isHuman = searchPlayerById(battleUnit.playerId)?.type == Player.Dto.PlayerTypeDto.HUMAN
        val isItsTurn = searchBattle()?.takeIf { battle -> !battle.isFinished }?.currentPlayerId == battleUnit.playerId
        return isHuman && isItsTurn && canCastAbility(battleUnitId, abilityId)
    }

    private fun castGroupsWhereCanCast(
        battleUnitId: String,
        abilityId: String,
    ): List<Dto.CastGroupDto> {
        val castGroups = whereCanCast(battleUnitId, abilityId)
        return castGroups
            .map { castGroup ->
                Dto.CastGroupDto(
                    tiles =
                        castGroup.positions
                            .map { position -> TileDto(row = position.row, column = position.column) },
                )
            }.distinct()
    }
}
