package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchSellPrice

class SearchSellRefusal(
    private val inventoryRepository: InventoryRepository,
    private val searchSellPrice: SearchSellPrice,
    private val tradeRule: TradeRule,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ): TradeRefusalDto? {
        try {
            tradeRule.requireTrading(battleUnitId)
        } catch (_: NotTradingWithAMerchant) {
            return TradeRefusalDto.NOT_TRADING
        }
        val sellPrice = searchSellPrice(lootableItemId) ?: return TradeRefusalDto.NOT_STORED
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return TradeRefusalDto.NOT_TRADING
        return inventory.sellRefusal(lootableItemId, sellPrice)
    }
}
