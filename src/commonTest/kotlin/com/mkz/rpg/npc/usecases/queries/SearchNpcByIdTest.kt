package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchNpcByIdTest {
    private val npcRepository = InMemoryNpcRepository()
    private val searchNpcById = SearchNpcById(npcRepository)

    @Test
    fun `should return the npc when it exists`() {
        // Given
        val npc = npc(id = "the-guy")
        npcRepository.save(npc)
        // When
        val storedNpc = searchNpcById("the-guy")
        // Then
        storedNpc shouldBe npc.toDto()
    }

    @Test
    fun `should return null when the npc does not exist`() {
        // When
        val storedNpc = searchNpcById("unknown")
        // Then
        storedNpc.shouldBeNull()
    }
}
