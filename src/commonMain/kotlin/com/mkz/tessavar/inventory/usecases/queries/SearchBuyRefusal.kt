package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotSoldByTheMerchant
import com.mkz.tessavar.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.TradeRule
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById

class SearchBuyRefusal(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val tradeRule: TradeRule,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ): TradeRefusalDto? {
        try {
            tradeRule.requireSoldByTheMerchant(battleUnitId, lootableItemId)
        } catch (_: NotTradingWithAMerchant) {
            return TradeRefusalDto.NOT_TRADING
        } catch (_: ItemNotSoldByTheMerchant) {
            return TradeRefusalDto.NOT_SOLD_BY_THE_MERCHANT
        }
        val lootableItem = searchLootableItemById(lootableItemId) ?: return TradeRefusalDto.NOT_SOLD_BY_THE_MERCHANT
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return TradeRefusalDto.NOT_TRADING
        return inventory.buyRefusal(lootableItem.price)
    }
}
