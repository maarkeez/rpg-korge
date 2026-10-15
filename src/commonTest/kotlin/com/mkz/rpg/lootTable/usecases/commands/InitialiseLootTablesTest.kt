package com.mkz.rpg.lootTable.usecases.commands

import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTable
import com.mkz.rpg.lootTable.domain.LootTableError.LootTableLootableItemDoesNotExist
import com.mkz.rpg.lootTable.domain.LootTableEvent.LootTableCreated
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootTable.usecases.services.FakeLootTableLoader
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseLootTablesTest {
    private val lootTableRepository = InMemoryLootTableRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val eventBus = FakeEventBus()

    private fun initialiseLootTables(loadedLootTables: List<LootTable.Dto>) =
        InitialiseLootTables(FakeLootTableLoader(loadedLootTables), lootTableRepository, SearchLootableItemById(lootableItemRepository), eventBus)

    @Test
    fun `should create every loaded loot table when loot tables are initialised`() {
        // Given
        lootableItemRepository.create(lootableItem(id = "health-potion"))
        val tierOne = lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion"), nothingEntry())).toDto()
        val tierTwo = lootTable(id = "tier-2", entries = listOf(nothingEntry())).toDto()
        val initialiseLootTables = initialiseLootTables(loadedLootTables = listOf(tierOne, tierTwo))
        // When
        initialiseLootTables()
        // Then
        lootTableRepository.searchById("tier-1")?.toDto() shouldBe tierOne
        lootTableRepository.searchById("tier-2")?.toDto() shouldBe tierTwo
        eventBus.shouldHavePublishedEvents(LootTableCreated("tier-1"), LootTableCreated("tier-2"))
    }

    @Test
    fun `should not create a loot table again when the loot table already exists`() {
        // Given
        val tierOne = lootTable(id = "tier-1", entries = listOf(nothingEntry(weight = 5)))
        lootTableRepository.create(tierOne)
        val initialiseLootTables = initialiseLootTables(loadedLootTables = listOf(lootTable(id = "tier-1", entries = listOf(nothingEntry(weight = 1))).toDto()))
        // When
        initialiseLootTables()
        // Then
        lootTableRepository.searchById("tier-1")?.toDto() shouldBe tierOne.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when a loot table lootable item does not exist`() {
        // Given
        val tierOne = lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "missing-lootable-item"))).toDto()
        val initialiseLootTables = initialiseLootTables(loadedLootTables = listOf(tierOne))
        // When
        val initialisation = { initialiseLootTables() }
        // Then
        shouldThrow<LootTableLootableItemDoesNotExist> { initialisation() }
    }
}
