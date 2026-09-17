package com.mkz.rpg.screen.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.domain.EventBus

class ConfirmCast(
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
            is DisplayAbilityCastPreview -> {
                eventBus.publish(
                    BattleUnitEvent.RequestCastAbility(
                        battleUnitId = battlefieldHud.battleUnitId,
                        abilityId = battlefieldHud.abilityId,
                        castGroup =
                            battlefieldHud.castGroup.tiles
                                .map { tile -> Battlefield.Dto.PositionDto(row = tile.row, column = tile.column) },
                    ),
                )
                val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }

            is DisplayAbilityCastRange,
            is DisplayMovementRange,
            is Idle,
            -> throw BattlefieldHudError.InvalidBattlefieldHudState()
        }
    }
}
