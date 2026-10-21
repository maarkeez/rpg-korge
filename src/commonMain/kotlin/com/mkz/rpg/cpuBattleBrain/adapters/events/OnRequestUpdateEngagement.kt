package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.RequestUpdateEngagement
import com.mkz.rpg.cpuBattleBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// The engagement is updated once the CPU battle units out of the battle have wandered, after the round finished.
class OnRequestUpdateEngagement(
    updateEngagement: UpdateEngagement,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<RequestUpdateEngagement> { event ->
            updateEngagement(battleUnitIds = event.battleUnitIds)
        }
}
