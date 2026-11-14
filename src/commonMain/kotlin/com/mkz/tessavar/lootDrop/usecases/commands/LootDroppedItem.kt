package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.tessavar.lootDrop.domain.LootDropRepository
import com.mkz.tessavar.shared.domain.EventBus

class LootDroppedItem(
    private val lootDropRepository: LootDropRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val storedLootDrop =
            lootDropRepository.searchAll().firstOrNull { lootDrop -> lootableItemId in lootDrop.droppedItems(row, column) } ?: throw DroppedItemNotFound()
        val (events, lootDrop) = storedLootDrop.loot(lootableItemId, row, column).pullEvents()
        lootDropRepository.update(lootDrop)
        eventBus.publish(events)
    }
}
