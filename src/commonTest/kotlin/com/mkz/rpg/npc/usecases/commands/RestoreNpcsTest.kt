package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class RestoreNpcsTest {
    private val npcRepository = InMemoryNpcRepository()
    private val restoreNpcs = RestoreNpcs(npcRepository, SearchLootableItemById(InMemoryLootableItemRepository().apply { create(lootableItem(id = "health-potion")) }))

    @Test
    fun `should rebuild the same state when it restores the snapshot it took`() {
        // Given: the elder was recruited meanwhile, and another npc placed.
        val npcs = listOf(npc(id = "elder", row = 1, column = 0, unitId = "mage"), npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion")))
        npcRepository.save(npc(id = "guard"))
        // When
        restoreNpcs(npcs.map { npc -> npc.toDto() })
        // Then
        npcRepository.searchAll().shouldContainExactlyInAnyOrder(npcs)
    }

    @Test
    fun `should fail when a merchant sells a lootable item the game no longer has`() {
        // When
        val result = runCatching { restoreNpcs(listOf(npc(stock = listOf("old-sword")).toDto())) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "old-sword"
    }
}
