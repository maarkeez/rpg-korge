package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

/** Removes a dropped item from its tile once an inventory has stored it, and requested its pick-up, from the first unit that dropped it there. */
class PickUpDroppedItem(
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
        val (events, lootDrop) = storedLootDrop.pickUp(lootableItemId, row, column).pullEvents()
        lootDropRepository.update(lootDrop)
        eventBus.publish(events)
    }
}
