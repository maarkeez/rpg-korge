package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnExplorationSecondStarted(
    wanderWhileExploring: WanderWhileExploring,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<PartyEvent.ExplorationSecondStarted> { event ->
            wanderWhileExploring(exploringPlayerId = event.playerId)
        }
}
