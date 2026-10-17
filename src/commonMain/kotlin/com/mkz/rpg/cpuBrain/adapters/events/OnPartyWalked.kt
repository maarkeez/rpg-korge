package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.cpuBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPartyWalked(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<ExplorationEvent.PartyWalked> { event ->
            engageEnemies(exploringPlayerId = event.playerId)
        }
}
