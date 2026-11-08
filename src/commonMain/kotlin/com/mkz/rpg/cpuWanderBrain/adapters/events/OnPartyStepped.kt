package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPartyStepped(
    respawnDefeatedBattleUnits: RespawnDefeatedBattleUnits,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<PartyEvent.PartyStepped> {
            respawnDefeatedBattleUnits()
        }
}
