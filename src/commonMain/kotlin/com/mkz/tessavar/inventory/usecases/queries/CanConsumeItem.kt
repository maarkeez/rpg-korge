package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.ConsumptionRule
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById

class CanConsumeItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val consumptionRule: ConsumptionRule,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ): Boolean {
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return false
        val consumable = searchLootableItemById(lootableItemId)?.lootType?.consumable ?: return false
        return inventory.canConsume(lootableItemId) && consumptionRule.canConsume(battleUnitId, consumable)
    }
}
