package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Pays a reward to a battle unit, requesting to drop what does not fit in its bag on the tile it stands on. A battle unit with no inventory
 * receives nothing. It is not limited to the player's turn, as a reward can be earned in a battle.
 */
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
