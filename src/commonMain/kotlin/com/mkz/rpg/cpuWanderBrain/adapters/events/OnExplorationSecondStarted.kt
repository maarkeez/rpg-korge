package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnExplorationSecondStarted(
    wanderWhileExploring: WanderWhileExploring,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<ExplorationEvent.ExplorationSecondStarted> { event ->
            wanderWhileExploring(exploringPlayerId = event.playerId)
        }
}
