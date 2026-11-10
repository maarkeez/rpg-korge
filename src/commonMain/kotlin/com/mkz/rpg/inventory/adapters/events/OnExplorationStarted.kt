package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
