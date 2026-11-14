package com.mkz.tessavar.inventory.usecases.services

import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.tessavar.npc.domain.NpcMother.npc
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.usecases.InMemoryQueries

class TradeMother {
    val queries = InMemoryQueries()
    val inventoryRepository = InMemoryInventoryRepository()
    val tradeRule = TradeRule(queries.searchParty, queries.searchNpcById)

    init {
        queries.lootableItemRepository.create(lootableItem(id = "health-potion", price = 10))
        queries.lootableItemRepository.create(lootableItem(id = "hammer", price = 50))
        queries.lootableItemRepository.create(lootableItem(id = "mana-potion", price = 10))
        queries.npcRepository.save(npc(id = "merchant", stock = listOf("health-potion", "hammer")))
        queries.npcRepository.save(npc(id = "the-guy"))
        talkTo("merchant")
        inventoryRepository.create(inventory(battleUnitId = "rat-1", gold = 100))
    }

    fun talkTo(npcId: String) = queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "viking-1")).talk(npcId).pullEvents().second)

    fun explore() = queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "viking-1")))

    fun gold(battleUnitId: String): Int? = inventoryRepository.searchByBattleUnitId(battleUnitId)?.toDto()?.gold

    fun storedItems(battleUnitId: String): List<String>? = inventoryRepository.searchByBattleUnitId(battleUnitId)?.toDto()?.storedItems
}
