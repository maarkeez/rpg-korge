package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.EventBus

/**
 * Displays again the selected battle unit with its current tile and movement range, e.g.: after it moves or casts,
 * or after a tile is occupied or vacated, since that may open or close paths.
 *
 * Out of the battles (e.g.: while exploring), the selected battle unit has no movement range, so it is only displayed
 * again when its tile changes. Otherwise, the CPU battle units wandering every second would display its info again in
 * place of its inventory.
 */
class RefreshMovementRange(
    private val searchPosition: SearchPosition,
    private val searchBattle: SearchBattle,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
    private val movementService: MovementService,
) {
    operator fun invoke() {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
            is DisplayMovementRange -> {
                val battleUnitId = battlefieldHud.battleUnitId
                val position = searchPosition(battleUnitId)
                if (position == null) {
                    // The selected battle unit is no longer on the battlefield, e.g.: it has been defeated.
                    val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                    return
                }
                val tile = TileDto(position.row, position.column)
                val isBattleInProgress = searchBattle()?.isFinished == false
                if (!isBattleInProgress && tile == battlefieldHud.tile) return
                val tilesWhereCanBeMoved = if (isBattleInProgress) movementService.tilesWhereCanMove(battleUnitId) else emptySet()
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .tilesWhereCanBeMoved(tile = tile, tilesWhereCanBeMoved = tilesWhereCanBeMoved)
                        .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
            is Idle,
            is DisplayAbilityCastRange,
            is DisplayAbilityCastPreview,
            -> return
        }
    }
}
