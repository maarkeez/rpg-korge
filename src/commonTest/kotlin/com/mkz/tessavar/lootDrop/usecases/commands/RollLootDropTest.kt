package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.ItemDropped
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.tessavar.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.tessavar.lootTable.usecases.queries.SearchLootTableById
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.FixedRandom
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RollLootDropTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val lootTableRepository = InMemoryLootTableRepository()
    private val eventBus = FakeEventBus()

    private fun givenABatLootDrop() {
        lootTableRepository.create(lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion", weight = 10), nothingEntry(weight = 5))))
        lootDropRepository.create(lootDrop(unitId = "bat", lootTableIds = listOf("tier-1")))
    }

    private fun rollLootDrop(roll: Int) = RollLootDrop(lootDropRepository, SearchLootTableById(lootTableRepository), FixedRandom(roll), eventBus)

    @Test
    fun `should drop the rolled lootable item on the given tile when the unit has a loot drop`() {
        // Given
        givenABatLootDrop()
        val rollLootDrop = rollLootDrop(roll = 0)
        // When
        rollLootDrop(unitId = "bat", row = 2, column = 3)
        // Then
        lootDropRepository.searchByUnitId("bat")!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 2, column = 3) to listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(ItemDropped(lootableItemId = "health-potion", row = 2, column = 3))
    }

    @Test
    fun `should not drop an item when nothing is rolled`() {
        // Given
        givenABatLootDrop()
        val rollLootDrop = rollLootDrop(roll = 14)
        // When
        rollLootDrop(unitId = "bat", row = 2, column = 3)
        // Then
        lootDropRepository
            .searchByUnitId("bat")!!
            .toDto()
            .droppedItems
            .shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not drop an item when the unit has no loot drop`() {
        // Given
        val rollLootDrop = rollLootDrop(roll = 0)
        // When
        rollLootDrop(unitId = "rat", row = 2, column = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
