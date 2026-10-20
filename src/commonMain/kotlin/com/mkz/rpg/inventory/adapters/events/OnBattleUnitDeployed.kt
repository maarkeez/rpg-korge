package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

// A deployed battle unit of the human player gets its inventory, e.g.: when the battle is set up, or an npc is recruited.
class OnBattleUnitDeployed(
    createInventory: CreateInventory,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDeployed> { event ->
            createInventory(battleUnitId = event.battleUnitId)
        }
}
