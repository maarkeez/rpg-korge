package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

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
 * Selects the battle unit with its movement range, as if the player had selected its tile, replacing any selection,
 * e.g.: the party leader when a human turn starts. A battle unit off the battlefield is not selected: the hud goes idle.
 */
class SelectBattleUnit(
    private val searchPosition: SearchPosition,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
    private val movementService: MovementService,
) {
    operator fun invoke(battleUnitId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        val idleBattlefieldHud =
            when (battlefieldHud) {
                is Idle -> battlefieldHud
                is DisplayMovementRange -> battlefieldHud.idle()
                is DisplayAbilityCastRange -> battlefieldHud.idle()
                is DisplayAbilityCastPreview -> battlefieldHud.idle()
            }
        val position = searchPosition(battleUnitId)
        val (events, updatedBattlefieldHud) =
            if (position == null) {
                idleBattlefieldHud.idle().pullEvents()
            } else {
                idleBattlefieldHud
                    .selectBattleUnit(
                        tile = TileDto(row = position.row, column = position.column),
                        battleUnitId = battleUnitId,
                        tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId),
                    ).pullEvents()
            }
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }
}
