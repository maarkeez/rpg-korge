package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.inventory.domain.InventoryEvent.ItemBought
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordItemBought

class OnItemBought(
    recordItemBought: RecordItemBought,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ItemBought> { event ->
            recordItemBought(lootableItemId = event.lootableItemId, price = event.price)
        }
}
