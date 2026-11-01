package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.rpg.inventory.usecases.commands.GrantReward
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestGrantReward(
    grantReward: GrantReward,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestGrantReward> { event ->
            grantReward(battleUnitId = event.battleUnitId, gold = event.gold, lootableItemIds = event.lootableItemIds, row = event.row, column = event.column)
        }
}
