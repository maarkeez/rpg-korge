package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.domain.EventBus

/** Clears any selection, e.g.: when a turn starts, so a previous selection can not be acted on. */
class DeselectBattleUnit(
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        val (events, updatedBattlefieldHud) =
            when (battlefieldHud) {
                is Idle -> battlefieldHud.idle().pullEvents()
                is DisplayMovementRange -> battlefieldHud.idle().pullEvents()
                is DisplayAbilityCastRange -> battlefieldHud.idle().pullEvents()
                is DisplayAbilityCastPreview -> battlefieldHud.idle().pullEvents()
            }
        battlefieldHudRepository.update(updatedBattlefieldHud)
        eventBus.publish(events)
    }
}
