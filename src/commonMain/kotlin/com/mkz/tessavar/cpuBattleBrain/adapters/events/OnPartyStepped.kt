package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnPartyStepped(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<PartyEvent.PartyStepped> { event ->
            engageEnemies(exploringPlayerId = event.playerId)
        }
}
