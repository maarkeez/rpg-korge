package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.usecases.commands.CreateInventory
import com.mkz.tessavar.party.domain.PartyEvent.ExplorationStarted
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnExplorationStarted(
    createInventory: CreateInventory,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ExplorationStarted> { event ->
            event.battleUnitIds.zip(event.unitIds).forEach { (battleUnitId, unitId) ->
                createInventory(battleUnitId = battleUnitId, unitId = unitId)
            }
        }
}
