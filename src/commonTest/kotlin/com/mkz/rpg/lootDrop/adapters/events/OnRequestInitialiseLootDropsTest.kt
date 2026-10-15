package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.rpg.lootDrop.usecases.services.FakeLootDropLoader
import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
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
