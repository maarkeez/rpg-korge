package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DropLootTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val lootTableRepository = InMemoryLootTableRepository()
    private val eventBus = FakeEventBus()

    // The bat rolls on the tier 1 loot table: the health potion drops on rolls 0 to 9, and nothing drops on rolls 10 to 14.
    private fun givenABatLootDrop() {
        lootTableRepository.create(lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion", weight = 10), nothingEntry(weight = 5))))
        lootDropRepository.create(lootDrop(unitId = "bat", lootTableIds = listOf("tier-1")))
    }

    private fun dropLoot(roll: Int) = DropLoot(lootDropRepository, SearchLootTableById(lootTableRepository), FixedRandom(roll), eventBus)

    @Test
    fun `should drop the rolled lootable item on the given tile when the unit has a loot drop`() {
        // Given
        givenABatLootDrop()
        val dropLoot = dropLoot(roll = 0)
        // When
        dropLoot(unitId = "bat", row = 2, column = 3)
        // Then
        lootDropRepository.searchByUnitId("bat")!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 2, column = 3) to listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(LootDropped(lootableItemId = "health-potion", row = 2, column = 3))
    }

    @Test
    fun `should not drop loot when nothing is rolled`() {
        // Given
        givenABatLootDrop()
        val dropLoot = dropLoot(roll = 14)
        // When
        dropLoot(unitId = "bat", row = 2, column = 3)
        // Then
        lootDropRepository
            .searchByUnitId("bat")!!
            .toDto()
            .droppedItems
            .shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not drop loot when the unit has no loot drop`() {
        // Given
        val dropLoot = dropLoot(roll = 0)
        // When
        dropLoot(unitId = "rat", row = 2, column = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
