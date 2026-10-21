package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// Each battle unit of the party gets its inventory when the exploration starts, e.g.: when the battle is set up.
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
