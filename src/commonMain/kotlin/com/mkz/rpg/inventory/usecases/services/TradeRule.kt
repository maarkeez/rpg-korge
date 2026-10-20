package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.inventory.domain.InventoryError.ItemNotSoldByTheMerchant
import com.mkz.rpg.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.rpg.npc.usecases.queries.SearchNpcById
import com.mkz.rpg.party.usecases.queries.SearchParty

/**
 * A battle unit can only buy and sell as a party member, while the party talks to a merchant. It can only buy the items
 * the merchant sells.
 */
class TradeRule(
    private val searchParty: SearchParty,
    private val searchNpcById: SearchNpcById,
) {
    fun requireTrading(battleUnitId: String) {
        merchantStock(battleUnitId)
    }

    fun requireSoldByTheMerchant(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        if (lootableItemId !in merchantStock(battleUnitId)) throw ItemNotSoldByTheMerchant()
    }

    // The items sold by the merchant the party talks to.
    private fun merchantStock(battleUnitId: String): List<String> {
        val party = searchParty() ?: throw NotTradingWithAMerchant()
        if (battleUnitId !in party.battleUnitIds) throw NotTradingWithAMerchant()
        val merchant = party.talkingNpcId?.let { npcId -> searchNpcById(npcId) } ?: throw NotTradingWithAMerchant()
        if (merchant.stock.isEmpty()) throw NotTradingWithAMerchant()
        return merchant.stock
    }
}
