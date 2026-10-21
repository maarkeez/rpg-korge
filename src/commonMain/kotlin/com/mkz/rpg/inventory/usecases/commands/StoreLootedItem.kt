package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Stores an item lying on a tile in the inventory of a battle unit, and requests the battlefield to pick it up from its tile. CPU battle units
 * and summoned battle units have none, so the item stays on its tile.
 */
class StoreLootedItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return
        searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val (events, updatedInventory) = inventory.store(lootableItemId, row, column).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
