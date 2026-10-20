package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.domain.WanderingEvent

// The engagement is updated once the CPU battle units out of the battle have wandered, after the round finished.
class OnBattleUnitsWanderedAfterRound(
    updateEngagement: UpdateEngagement,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<WanderingEvent.BattleUnitsWanderedAfterRound> { event ->
            updateEngagement(battleUnitIds = event.battleUnitIds)
        }
}
