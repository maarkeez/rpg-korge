package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.usecases.InMemoryQueries

/** The knight, the viking and the mage explore, in that party order. The rat is no party member. */
class GiveMother {
    val queries = InMemoryQueries()
    val inventoryRepository = InMemoryInventoryRepository()
    val giveRule = GiveRule(queries.searchParty)

    init {
        explore(listOf("knight-1", "viking-1", "mage-1"))
    }

    fun explore(battleUnitIds: List<String>) = queries.partyRepository.save(party(battleUnitIds = battleUnitIds))

    fun talkTo(npcId: String) = queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "viking-1", "mage-1")).talk(npcId).pullEvents().second)

    fun battle() = queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "viking-1", "mage-1")).pause().pullEvents().second)

    fun storedItems(battleUnitId: String): List<String>? = inventoryRepository.searchByBattleUnitId(battleUnitId)?.toDto()?.storedItems
}
