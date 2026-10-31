package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityInfo
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError.InvalidBattlefieldHudState
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Selects an ability of the selected battle unit. It is cast only in a battle in progress, when the battle unit belongs
 * to the human player whose turn it is and can cast it: its cast range is displayed. Otherwise (e.g.: in cooldown, a CPU
 * battle unit's, or while exploring), only what it does is displayed. Selecting the ability on show again deselects it.
 */
class ProcessAbilitySelected(
    private val canCastAbility: CanCastAbility,
    private val whereCanCast: WhereCanCast,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchBattle: SearchBattle,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        val (events, updatedBattlefieldHud) =
            when (battlefieldHud) {
                is DisplayMovementRange ->
                    if (canCast(battlefieldHud.battleUnitId, abilityId)) {
                        battlefieldHud.selectAbility(abilityId, castGroupsWhereCanCast(battlefieldHud.battleUnitId, abilityId)).pullEvents()
                    } else {
                        battlefieldHud.displayAbilityInfo(abilityId).pullEvents()
                    }

                is DisplayAbilityCastRange ->
                    if (abilityId == battlefieldHud.abilityId) {
                        battlefieldHud.deselectAbility().pullEvents()
                    } else if (canCast(battlefieldHud.battleUnitId, abilityId)) {
                        battlefieldHud.selectAbility(abilityId, castGroupsWhereCanCast(battlefieldHud.battleUnitId, abilityId)).pullEvents()
                    } else {
                        battlefieldHud.displayAbilityInfo(abilityId).pullEvents()
                    }

                is DisplayAbilityInfo ->
                    if (abilityId == battlefieldHud.abilityId) {
                        battlefieldHud.hideAbilityInfo().pullEvents()
                    } else if (canCast(battlefieldHud.battleUnitId, abilityId)) {
                        battlefieldHud.selectAbility(abilityId, castGroupsWhereCanCast(battlefieldHud.battleUnitId, abilityId)).pullEvents()
                    } else {
                        battlefieldHud.displayAbilityInfo(abilityId).pullEvents()
                    }

                is Idle,
                is DisplayAbilityCastPreview,
                -> throw InvalidBattlefieldHudState()
            }
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }

    // Only the human player casts, on its turn, with its own battle units. A finished battle keeps the turn it ended on,
    // so it is no one's turn while exploring.
    private fun canCast(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val battleUnit = searchBattleUnitById(battleUnitId) ?: return false
        val isHuman = searchPlayerById(battleUnit.playerId)?.type == Player.Dto.PlayerTypeDto.HUMAN
        val isItsTurn = searchBattle()?.takeIf { battle -> !battle.isFinished }?.currentPlayerTurn == battleUnit.playerId
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
