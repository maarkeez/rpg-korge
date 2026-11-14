package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.usecases.commands.CreateInventory
import com.mkz.tessavar.party.domain.PartyEvent.PartyJoined
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
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
