package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchNpcsSnapshotTest {
    private val npcRepository = InMemoryNpcRepository()
    private val searchNpcsSnapshot = SearchNpcsSnapshot(npcRepository)

    @Test
    fun `should return every npc with its tile and its stock`() {
        // Given
        val npcs = listOf(npc(id = "elder", row = 1, column = 0, unitId = "mage"), npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion")))
        npcs.forEach(npcRepository::save)
        // When
        val result = searchNpcsSnapshot()
        // Then
        result shouldContainExactlyInAnyOrder npcs.map { npc -> npc.toDto() }
    }

    @Test
    fun `should return nothing when there is no npc`() {
        // When
        val result = searchNpcsSnapshot()
        // Then
        result.shouldBeEmpty()
    }
}
