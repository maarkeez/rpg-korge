package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryError
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById

/** Whether the battle unit can buy the item from the merchant the party talks to: it can pay its price, and has space to store it. */
class CanBuyItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val tradeRule: TradeRule,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ): Boolean {
        try {
            tradeRule.requireSoldByTheMerchant(battleUnitId, lootableItemId)
        } catch (_: InventoryError) {
            return false
        }
        val lootableItem = searchLootableItemById(lootableItemId) ?: return false
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return false
        return inventory.canBuy(lootableItem.price)
    }
}
