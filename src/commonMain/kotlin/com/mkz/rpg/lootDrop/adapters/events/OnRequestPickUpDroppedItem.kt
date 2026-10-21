package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestPickUpDroppedItem
import com.mkz.rpg.lootDrop.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestPickUpDroppedItem(
    pickUpDroppedItem: PickUpDroppedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestPickUpDroppedItem> { event ->
            pickUpDroppedItem(lootableItemId = event.lootableItemId, row = event.row, column = event.column)
        }
}
