package com.mkz.rpg.telemetry.adapters.events

import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.telemetry.usecases.commands.RecordItemBought

class OnItemBought(
    recordItemBought: RecordItemBought,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ItemBought> { event ->
            recordItemBought(lootableItemId = event.lootableItemId, price = event.price)
        }
}
