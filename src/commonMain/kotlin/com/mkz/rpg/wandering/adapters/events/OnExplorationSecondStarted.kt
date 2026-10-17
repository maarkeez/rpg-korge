package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.usecases.commands.WanderWhileExploring

class OnExplorationSecondStarted(
    wanderWhileExploring: WanderWhileExploring,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<ExplorationEvent.ExplorationSecondStarted> { event ->
            wanderWhileExploring(exploringPlayerId = event.playerId)
        }
}
