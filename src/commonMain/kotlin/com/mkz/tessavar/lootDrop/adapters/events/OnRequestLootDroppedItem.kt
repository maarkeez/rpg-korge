package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.lootDrop.domain.LootDropEvent.RequestLootDroppedItem
import com.mkz.tessavar.lootDrop.usecases.commands.LootDroppedItem
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestLootDroppedItem(
    lootDroppedItem: LootDroppedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<RequestLootDroppedItem> { event ->
            lootDroppedItem(lootableItemId = event.lootableItemId, row = event.row, column = event.column)
        }
}
