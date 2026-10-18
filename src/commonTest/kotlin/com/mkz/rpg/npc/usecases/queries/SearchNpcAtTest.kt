package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchNpcAtTest {
    private val npcRepository = InMemoryNpcRepository()
    private val searchNpcAt = SearchNpcAt(npcRepository)

    @Test
    fun `should return the npc standing on the tile`() {
        // Given
        val theGuy = npc(id = "the-guy", row = 10, column = 2)
        npcRepository.save(theGuy)
        npcRepository.save(npc(id = "the-girl", row = 3, column = 4))
        // When
        val npc = searchNpcAt(row = 10, column = 2)
        // Then
        npc shouldBe theGuy.toDto()
    }

    @Test
    fun `should return null when no npc stands on the tile`() {
        // Given
        npcRepository.save(npc(id = "the-guy", row = 10, column = 2))
        // When
        val npc = searchNpcAt(row = 10, column = 3)
        // Then
        npc.shouldBeNull()
    }
}
