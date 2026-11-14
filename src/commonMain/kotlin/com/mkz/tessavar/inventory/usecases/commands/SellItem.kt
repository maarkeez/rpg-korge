package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotStored
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.TradeRule
import com.mkz.tessavar.lootableItem.usecases.queries.SearchSellPrice
import com.mkz.tessavar.shared.domain.EventBus

class SellItem(
    private val inventoryRepository: InventoryRepository,
    private val searchSellPrice: SearchSellPrice,
    private val tradeRule: TradeRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        tradeRule.requireTrading(battleUnitId)
        val sellPrice = searchSellPrice(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw ItemNotStored()
        val (events, updatedInventory) = inventory.sell(lootableItemId, sellPrice).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
