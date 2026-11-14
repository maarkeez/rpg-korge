package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDropEvent
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.tessavar.lootDrop.usecases.services.FakeLootDropLoader
import com.mkz.tessavar.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.usecases.queries.SearchLootTableById
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitMother.unit
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseLootDropsTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val unitRepository = InMemoryUnitRepository().also { it.create(unit(id = "bat")) }
    private val lootTableRepository = InMemoryLootTableRepository().also { it.create(lootTable(id = "tier-1")) }
    private val batLootDrop = lootDrop(unitId = "bat", lootTableIds = listOf("tier-1")).toDto()
    private val onRequestInitialiseLootDrops =
        OnRequestInitialiseLootDrops(
            InitialiseLootDrops(FakeLootDropLoader(lootDrops = listOf(batLootDrop)), lootDropRepository, SearchUnitById(unitRepository), SearchLootTableById(lootTableRepository), eventBus),
            eventBus,
        )

    @Test
    fun `should initialise the loot drops when the request is dispatched`() {
        // Given
        eventBus.publish(LootDropEvent.RequestInitialiseLootDrops)
        // When
        eventBus.dispatch()
        // Then
        lootDropRepository.searchByUnitId("bat")?.toDto() shouldBe batLootDrop
    }
}
