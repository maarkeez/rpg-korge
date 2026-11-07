package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestLootDroppedItem
import com.mkz.rpg.lootDrop.usecases.commands.LootDroppedItem
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestLootDroppedItem(
    lootDroppedItem: LootDroppedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestLootDroppedItem> { event ->
            lootDroppedItem(lootableItemId = event.lootableItemId, row = event.row, column = event.column)
        }
}
