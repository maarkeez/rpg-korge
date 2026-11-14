package com.mkz.tessavar.npc.adapters.save

import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.tessavar.npc.domain.NpcMother.npc
import com.mkz.tessavar.npc.domain.NpcRepository
import com.mkz.tessavar.npc.usecases.commands.LoadNpcsSnapshot
import com.mkz.tessavar.npc.usecases.queries.SearchNpcsSnapshot
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NpcSnapshottableTest {
    private val searchLootableItemById = SearchLootableItemById(InMemoryLootableItemRepository().apply { create(lootableItem(id = "health-potion")) })
    private val npcs = listOf(npc(id = "elder", row = 1, column = 0, unitId = "mage"), npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion")))

    private fun snapshottable(repository: NpcRepository) = NpcSnapshottable(SearchNpcsSnapshot(repository), LoadNpcsSnapshot(repository, searchLootableItemById))

    @Test
    fun `Load should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryNpcRepository().apply { npcs.forEach(::save) }).snapshot()
        val loadedRepository = InMemoryNpcRepository()
        // When
        snapshottable(loadedRepository).load(snapshot)
        // Then
        loadedRepository.searchAll().shouldContainExactlyInAnyOrder(npcs)
    }

    @Test
    fun `Key should be npc`() {
        // When
        val result = snapshottable(InMemoryNpcRepository()).key
        // Then
        result shouldBe "npc"
    }
}
