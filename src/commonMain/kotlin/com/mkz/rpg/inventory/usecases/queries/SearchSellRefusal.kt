package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchSellPrice

/**
 * Why the battle unit can not sell the item to the merchant the party talks to: it is not trading, it stores no copy of
 * the item, its only copy is equipped, or the sell price takes its gold above the most it can carry. `null` when it can
 * sell it.
 */
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
        // An item that does not exist can not be stored.
        val sellPrice = searchSellPrice(lootableItemId) ?: return TradeRefusalDto.NOT_STORED
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: return TradeRefusalDto.NOT_TRADING
        return inventory.sellRefusal(lootableItemId, sellPrice)
    }
}
