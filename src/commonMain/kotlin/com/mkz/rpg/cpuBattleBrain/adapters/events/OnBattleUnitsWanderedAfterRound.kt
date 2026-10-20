package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The engagement is updated once the CPU battle units out of the battle have wandered, after the round finished.
class OnBattleUnitsWanderedAfterRound(
    updateEngagement: UpdateEngagement,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<CpuWanderBrainEvent.BattleUnitsWanderedAfterRound> { event ->
            updateEngagement(battleUnitIds = event.battleUnitIds)
        }
}
