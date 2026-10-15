package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropUnitDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropCreated
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.services.FakeLootDropLoader
import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseLootDropsTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val unitRepository = InMemoryUnitRepository()
    private val lootTableRepository = InMemoryLootTableRepository()
    private val eventBus = FakeEventBus()

    private fun initialiseLootDrops(loadedLootDrops: List<LootDrop.Dto>) =
        InitialiseLootDrops(FakeLootDropLoader(loadedLootDrops), lootDropRepository, SearchUnitById(unitRepository), SearchLootTableById(lootTableRepository), eventBus)

    @Test
    fun `should create every loaded loot drop when loot drops are initialised`() {
        // Given
        unitRepository.create(unit(id = "bat"))
        unitRepository.create(unit(id = "rat"))
        lootTableRepository.create(lootTable(id = "tier-1"))
        val batLootDrop = lootDrop(unitId = "bat", lootTableIds = listOf("tier-1")).toDto()
        val ratLootDrop = lootDrop(unitId = "rat", lootTableIds = listOf("tier-1")).toDto()
        val initialiseLootDrops = initialiseLootDrops(loadedLootDrops = listOf(batLootDrop, ratLootDrop))
        // When
        initialiseLootDrops()
        // Then
        lootDropRepository.searchByUnitId("bat")?.toDto() shouldBe batLootDrop
        lootDropRepository.searchByUnitId("rat")?.toDto() shouldBe ratLootDrop
        eventBus.shouldHavePublishedEvents(LootDropCreated("bat"), LootDropCreated("rat"))
    }

    @Test
    fun `should not create a loot drop again when the loot drop of the unit already exists`() {
        // Given
        val batLootDrop = lootDrop(unitId = "bat", lootTableIds = listOf("tier-1"))
        lootDropRepository.create(batLootDrop)
        val initialiseLootDrops = initialiseLootDrops(loadedLootDrops = listOf(lootDrop(unitId = "bat", lootTableIds = listOf("tier-2")).toDto()))
        // When
        initialiseLootDrops()
        // Then
        lootDropRepository.searchByUnitId("bat")?.toDto() shouldBe batLootDrop.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the loot drop unit does not exist`() {
        // Given
        lootTableRepository.create(lootTable(id = "tier-1"))
        val initialiseLootDrops = initialiseLootDrops(loadedLootDrops = listOf(lootDrop(unitId = "missing-unit", lootTableIds = listOf("tier-1")).toDto()))
        // When
        val initialisation = { initialiseLootDrops() }
        // Then
        shouldThrow<LootDropUnitDoesNotExist> { initialisation() }
    }

    @Test
    fun `should fail when a loot drop loot table does not exist`() {
        // Given
        unitRepository.create(unit(id = "bat"))
        val initialiseLootDrops = initialiseLootDrops(loadedLootDrops = listOf(lootDrop(unitId = "bat", lootTableIds = listOf("missing-loot-table")).toDto()))
        // When
        val initialisation = { initialiseLootDrops() }
        // Then
        shouldThrow<LootDropLootTableDoesNotExist> { initialisation() }
    }
}
