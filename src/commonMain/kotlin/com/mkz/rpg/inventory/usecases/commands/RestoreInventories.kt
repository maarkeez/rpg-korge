package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById

/** Replaces every inventory by the ones a save took, publishing no events. Fails when an item is a lootable item the game no longer has. */
class RestoreInventories(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
) {
    operator fun invoke(inventories: List<Inventory.Dto>) {
        inventories.flatMap(Inventory.Dto::storedItems).forEach { lootableItemId -> requireNotNull(searchLootableItemById(lootableItemId)) { "Unknown lootable item $lootableItemId" } }
        inventoryRepository.deleteAll()
        inventories.forEach { inventory -> inventoryRepository.create(Inventory.restore(inventory)) }
    }
}
