package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.usecases.commands.StoreLootedItem
import com.mkz.rpg.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
