package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryError
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById

/**
 * Whether the battle unit can sell the item to the merchant the party talks to: it stores a copy that is not equipped,
 * and its sell price does not take the gold above the most it can carry.
 */
class CanSellItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val tradeRule: TradeRule,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ): Boolean {
        try {
            tradeRule.requireTrading(battleUnitId)
        } catch (_: InventoryError) {
            return false
        }
        val lootableItem = searchLootableItemById(lootableItemId) ?: return false
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return false
        return inventory.canSell(lootableItemId, lootableItem.sellPrice)
    }
}
