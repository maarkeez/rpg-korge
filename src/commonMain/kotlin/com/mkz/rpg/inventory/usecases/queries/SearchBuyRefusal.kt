package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotSoldByTheMerchant
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById

/**
 * Why the battle unit can not buy the item from the merchant the party talks to: it is not trading, the merchant does not
 * sell it, or the battle unit can not pay its price or has no space to store it. `null` when it can buy it.
 */
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
