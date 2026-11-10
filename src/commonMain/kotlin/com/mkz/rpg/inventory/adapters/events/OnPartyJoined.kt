package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.party.domain.PartyEvent.PartyJoined
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPartyJoined(
    createInventory: CreateInventory,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PartyJoined> { event ->
            createInventory(battleUnitId = event.battleUnitId, unitId = event.unitId)
        }
}
