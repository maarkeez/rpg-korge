package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.usecases.commands.CreateInventory
import com.mkz.tessavar.party.domain.PartyEvent.PartyJoined
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnPartyJoined(
    createInventory: CreateInventory,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PartyJoined> { event ->
            createInventory(battleUnitId = event.battleUnitId, unitId = event.unitId)
        }
}
