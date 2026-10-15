package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.usecases.commands.PlaceDroppedItem
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnLootDropped(
    placeDroppedItem: PlaceDroppedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<LootDropped> { event ->
            placeDroppedItem(lootableItemId = event.lootableItemId, row = event.row, column = event.column)
        }
}
