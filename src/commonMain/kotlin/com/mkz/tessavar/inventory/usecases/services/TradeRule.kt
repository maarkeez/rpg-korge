package com.mkz.tessavar.inventory.usecases.services

import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotSoldByTheMerchant
import com.mkz.tessavar.inventory.domain.InventoryError.NotTradingWithAMerchant
import com.mkz.tessavar.npc.usecases.queries.SearchNpcById
import com.mkz.tessavar.party.usecases.queries.SearchParty

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

    private fun merchantStock(battleUnitId: String): List<String> {
        val party = searchParty() ?: throw NotTradingWithAMerchant()
        if (battleUnitId !in party.battleUnitIds) throw NotTradingWithAMerchant()
        val merchant = party.talkingNpcId?.let { npcId -> searchNpcById(npcId) } ?: throw NotTradingWithAMerchant()
        if (merchant.stock.isEmpty()) throw NotTradingWithAMerchant()
        return merchant.stock
    }
}
