package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.party.domain.PartyEvent.PartyJoined
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnPartyJoinedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val onPartyJoined = OnPartyJoined(CreateInventory(inventoryRepository, queries.searchUnitById), eventBus)

    @Test
    fun `should create the inventory of the battle unit when it joins the party`() {
        // Given
        queries.unitRepository.create(unit(id = "viking", startingGold = 100))
        eventBus.publish(PartyJoined(playerId = "human", battleUnitId = "viking-1", unitId = "viking"))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("viking-1")?.toDto()?.gold shouldBe 100
    }
}
