package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.tessavar.inventory.usecases.commands.GrantReward
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestGrantReward(
    grantReward: GrantReward,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestGrantReward> { event ->
            grantReward(battleUnitId = event.battleUnitId, gold = event.gold, lootableItemIds = event.lootableItemIds, row = event.row, column = event.column)
        }
}
