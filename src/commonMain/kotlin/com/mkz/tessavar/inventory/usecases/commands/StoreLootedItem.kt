package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.shared.domain.EventBus

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
