package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordItemSold

class OnItemSold(
    recordItemSold: RecordItemSold,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ItemSold> { event ->
            recordItemSold(lootableItemId = event.lootableItemId, sellPrice = event.sellPrice)
        }
}
