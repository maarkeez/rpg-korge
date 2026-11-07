package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

/** Takes a dropped item off its tile once a party member has looted it, from the first unit that dropped it there. */
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
