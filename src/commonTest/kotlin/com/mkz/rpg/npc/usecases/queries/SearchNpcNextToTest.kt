package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchNpcNextToTest {
    private val npcRepository = InMemoryNpcRepository()
    private val searchNpcNextTo = SearchNpcNextTo(npcRepository)

    @Test
    fun `should return the npc standing next to the tile`() {
        // Given
        val theGuy = npc(id = "the-guy", row = 10, column = 1)
        npcRepository.save(theGuy)
        npcRepository.save(npc(id = "the-girl", row = 3, column = 4))
        // When
        val npc = searchNpcNextTo(row = 10, column = 2)
        // Then
        npc shouldBe theGuy.toDto()
    }

    @Test
    fun `should return the first npc next to the tile, north, east, south and west, in that order`() {
        // Given
        npcRepository.save(npc(id = "the-west-guy", row = 10, column = 1))
        npcRepository.save(npc(id = "the-south-guy", row = 11, column = 2))
        npcRepository.save(npc(id = "the-east-guy", row = 10, column = 3))
        // When
        val npc = searchNpcNextTo(row = 10, column = 2)
        // Then
        npc?.id shouldBe "the-east-guy"
    }

    @Test
    fun `should return null when the npc stands on a diagonal of the tile`() {
        // Given
        npcRepository.save(npc(id = "the-guy", row = 9, column = 1))
        // When
        val npc = searchNpcNextTo(row = 10, column = 2)
        // Then
        npc.shouldBeNull()
    }

    @Test
    fun `should return null when the npc stands on the tile itself`() {
        // Given
        npcRepository.save(npc(id = "the-guy", row = 10, column = 2))
        // When
        val npc = searchNpcNextTo(row = 10, column = 2)
        // Then
        npc.shouldBeNull()
    }
}
