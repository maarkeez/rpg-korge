package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.tessavar.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.TradeRule
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.shared.domain.EventBus

class BuyItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val tradeRule: TradeRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        tradeRule.requireSoldByTheMerchant(battleUnitId, lootableItemId)
        val lootableItem = searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw NotTradingWithAMerchant()
        val (events, updatedInventory) = inventory.buy(lootableItemId, lootableItem.price).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
