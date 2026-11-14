package com.mkz.tessavar.inventory.adapters.events

import com.mkz.tessavar.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.tessavar.inventory.usecases.commands.CreateInventory
import com.mkz.tessavar.party.domain.PartyEvent.ExplorationStarted
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnExplorationStartedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val onExplorationStarted = OnExplorationStarted(CreateInventory(inventoryRepository, queries.searchUnitById), eventBus)

    @Test
    fun `should create the inventory of each battle unit of the party when the exploration starts`() {
        // Given
        queries.unitRepository.create(unit(id = "knight", startingGold = 20))
        queries.unitRepository.create(unit(id = "viking", startingGold = 100))
        eventBus.publish(ExplorationStarted(playerId = "human", battleUnitIds = listOf("knight-1", "viking-1"), unitIds = listOf("knight", "viking")))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.gold shouldBe 20
        inventoryRepository.searchByBattleUnitId("viking-1")?.toDto()?.gold shouldBe 100
    }
}
