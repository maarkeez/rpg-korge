package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnPartyStepped(
    respawnDefeatedBattleUnits: RespawnDefeatedBattleUnits,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<PartyEvent.PartyStepped> {
            respawnDefeatedBattleUnits()
        }
}
