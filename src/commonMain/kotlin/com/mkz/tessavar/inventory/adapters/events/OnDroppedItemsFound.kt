package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.usecases.commands.StoreLootedItem
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnDroppedItemsFound(
    storeLootedItem: StoreLootedItem,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<DroppedItemsFound> { event ->
            event.lootableItemIds.forEach { lootableItemId ->
                storeLootedItem(battleUnitId = event.battleUnitId, lootableItemId = lootableItemId, row = event.row, column = event.column)
            }
        }
}
