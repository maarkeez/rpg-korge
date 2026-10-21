package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Places an item dropped by a unit on a tile, where it lies until an inventory stores it, without rolling its loot tables, e.g.: to prepare a
 * scenario.
 */
class PlaceDroppedItem(
    private val lootDropRepository: LootDropRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        unitId: String,
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val storedLootDrop = lootDropRepository.searchByUnitId(unitId) ?: return
        val (events, lootDrop) = storedLootDrop.place(lootableItemId, row, column).pullEvents()
        lootDropRepository.update(lootDrop)
        eventBus.publish(events)
    }
}
