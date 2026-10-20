package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPartyWalked(
    engageEnemies: EngageEnemies,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<PartyEvent.PartyWalked> { event ->
            engageEnemies(exploringPlayerId = event.playerId)
        }
}
