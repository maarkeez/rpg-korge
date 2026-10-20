package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.EventBus

/** Buys an item from the merchant the party talks to: the battle unit pays its price, and stores it. */
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
