package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchSellPrice
import com.mkz.rpg.shared.domain.EventBus

/** Sells a stored item that is not equipped to the merchant the party talks to, for its sell price. The item leaves the game. */
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
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw InventoryItemNotFound()
        val (events, updatedInventory) = inventory.sell(lootableItemId, sellPrice).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
