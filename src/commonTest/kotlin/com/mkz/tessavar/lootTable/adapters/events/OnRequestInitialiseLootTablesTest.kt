package com.mkz.tessavar.lootTable.adapters.events

import com.mkz.tessavar.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.tessavar.lootTable.domain.LootTableEvent
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.tessavar.lootTable.usecases.commands.InitialiseLootTables
import com.mkz.tessavar.lootTable.usecases.services.FakeLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseLootTablesTest {
    private val eventBus = InMemoryEventBus()
    private val lootTableRepository = InMemoryLootTableRepository()
    private val lootableItemRepository = InMemoryLootableItemRepository().also { it.create(lootableItem(id = "health-potion")) }
    private val tierOne = lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion"))).toDto()
    private val onRequestInitialiseLootTables =
        OnRequestInitialiseLootTables(
            InitialiseLootTables(FakeLootTableLoader(lootTables = listOf(tierOne)), lootTableRepository, SearchLootableItemById(lootableItemRepository), eventBus),
            eventBus,
        )

    @Test
    fun `should initialise the loot tables when the request is dispatched`() {
        // Given
        eventBus.publish(LootTableEvent.RequestInitialiseLootTables)
        // When
        eventBus.dispatch()
        // Then
        lootTableRepository.searchById("tier-1")?.toDto() shouldBe tierOne
    }
}
