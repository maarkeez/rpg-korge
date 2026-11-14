package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.tessavar.npc.domain.NpcMother.npc
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class LoadNpcsSnapshotTest {
    private val npcRepository = InMemoryNpcRepository()
    private val loadNpcsSnapshot = LoadNpcsSnapshot(npcRepository, SearchLootableItemById(InMemoryLootableItemRepository().apply { create(lootableItem(id = "health-potion")) }))

    @Test
    fun `should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val npcs = listOf(npc(id = "elder", row = 1, column = 0, unitId = "mage"), npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion")))
        npcRepository.save(npc(id = "guard"))
        // When
        loadNpcsSnapshot(npcs.map { npc -> npc.toDto() })
        // Then
        npcRepository.searchAll().shouldContainExactlyInAnyOrder(npcs)
    }

    @Test
    fun `should fail when a merchant sells a lootable item the game no longer has`() {
        // When
        val result = runCatching { loadNpcsSnapshot(listOf(npc(stock = listOf("old-sword")).toDto())) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "old-sword"
    }
}
