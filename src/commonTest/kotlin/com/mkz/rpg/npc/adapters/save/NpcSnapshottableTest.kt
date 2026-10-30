package com.mkz.rpg.npc.adapters.save

import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class NpcSnapshottableTest {
    private val searchLootableItemById = SearchLootableItemById(InMemoryLootableItemRepository().apply { create(lootableItem(id = "health-potion")) })
    private val npcs = listOf(npc(id = "elder", row = 1, column = 0, unitId = "mage"), npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion")))

    private fun snapshot(): String = NpcSnapshottable(InMemoryNpcRepository().apply { npcs.forEach(::save) }, searchLootableItemById).snapshot()

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given: the elder was recruited meanwhile, and another npc placed.
        val restoredRepository = InMemoryNpcRepository().apply { save(npc(id = "guard")) }
        // When
        NpcSnapshottable(restoredRepository, searchLootableItemById).restore(snapshot())
        // Then
        restoredRepository.searchAll().shouldContainExactlyInAnyOrder(npcs)
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        val restoredRepository = InMemoryNpcRepository()
        // When
        NpcSnapshottable(restoredRepository, searchLootableItemById).restore(snapshot())
        // Then
        restoredRepository.searchAll().flatMap { npc -> npc.pullEvents().first }.shouldBeEmpty()
    }

    @Test
    fun `Restore should fail when a merchant sells a lootable item the game no longer has`() {
        // Given
        val snapshot = NpcSnapshottable(InMemoryNpcRepository().apply { save(npc(stock = listOf("old-sword"))) }, searchLootableItemById).snapshot()
        // When
        val result = runCatching { NpcSnapshottable(InMemoryNpcRepository(), searchLootableItemById).restore(snapshot) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "old-sword"
    }
}
