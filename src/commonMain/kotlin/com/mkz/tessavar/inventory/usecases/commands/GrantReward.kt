package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.shared.domain.EventBus

class GrantReward(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        gold: Int,
        lootableItemIds: List<String>,
        row: Int,
        column: Int,
    ) {
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return
        lootableItemIds.forEach { lootableItemId -> searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist() }
        val (events, updatedInventory) = inventory.grantReward(gold, lootableItemIds, row, column).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
