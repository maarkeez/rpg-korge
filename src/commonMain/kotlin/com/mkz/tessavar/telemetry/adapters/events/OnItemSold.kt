package com.mkz.tessavar.telemetry.adapters.events

import com.mkz.tessavar.inventory.domain.InventoryEvent.ItemSold
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.telemetry.usecases.commands.RecordItemSold

class OnItemSold(
    recordItemSold: RecordItemSold,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ItemSold> { event ->
            recordItemSold(lootableItemId = event.lootableItemId, sellPrice = event.sellPrice)
        }
}
