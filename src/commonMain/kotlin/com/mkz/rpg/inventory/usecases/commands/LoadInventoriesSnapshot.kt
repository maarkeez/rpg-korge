package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById

class LoadInventoriesSnapshot(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
) {
    operator fun invoke(inventories: List<Inventory.Dto>) {
        inventories.flatMap(Inventory.Dto::storedItems).forEach { lootableItemId -> requireNotNull(searchLootableItemById(lootableItemId)) { "Unknown lootable item $lootableItemId" } }
        inventoryRepository.deleteAll()
        inventories.forEach { inventory -> inventoryRepository.create(Inventory.load(inventory)) }
    }
}
