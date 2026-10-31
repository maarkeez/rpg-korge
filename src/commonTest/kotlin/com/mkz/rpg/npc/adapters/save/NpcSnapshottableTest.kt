package com.mkz.rpg.npc.adapters.save

import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.npc.usecases.commands.RestoreNpcs
import com.mkz.rpg.npc.usecases.queries.SearchNpcsSnapshot
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NpcSnapshottableTest {
    private val searchLootableItemById = SearchLootableItemById(InMemoryLootableItemRepository().apply { create(lootableItem(id = "health-potion")) })
    private val npcs = listOf(npc(id = "elder", row = 1, column = 0, unitId = "mage"), npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion")))

    private fun snapshottable(repository: NpcRepository) = NpcSnapshottable(SearchNpcsSnapshot(repository), RestoreNpcs(repository, searchLootableItemById))

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryNpcRepository().apply { npcs.forEach(::save) }).snapshot()
        val restoredRepository = InMemoryNpcRepository()
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.searchAll().shouldContainExactlyInAnyOrder(npcs)
    }

    @Test
    fun `Key should be npc`() {
        // When
        val result = snapshottable(InMemoryNpcRepository()).key
        // Then
        result shouldBe "npc"
    }
}
