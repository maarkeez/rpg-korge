package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Drops an item for a unit on a tile, where it lies until a party member loots it, without rolling its loot drop, e.g.: to prepare a
 * scenario.
 */
class DropItemWithoutRolling(
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
        val (events, lootDrop) = storedLootDrop.drop(lootableItemId, row, column).pullEvents()
        lootDropRepository.update(lootDrop)
        eventBus.publish(events)
    }
}
