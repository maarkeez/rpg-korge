package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnExplorationSecondStarted(
    wanderWhileExploring: WanderWhileExploring,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<PartyEvent.ExplorationSecondStarted> { event ->
            wanderWhileExploring(exploringPlayerId = event.playerId)
        }
}
