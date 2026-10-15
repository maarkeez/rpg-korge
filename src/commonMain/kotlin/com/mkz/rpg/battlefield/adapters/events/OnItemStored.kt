package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnItemStored(
    pickUpDroppedItem: PickUpDroppedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<ItemStored> { event ->
            pickUpDroppedItem(lootableItemId = event.lootableItemId, row = event.row, column = event.column)
        }
}
