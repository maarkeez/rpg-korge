package com.mkz.rpg.npc.adapters.storage

import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryNpcRepositoryTest {
    private val npcRepository = InMemoryNpcRepository()

    @Test
    fun `Save should store the npc`() {
        // Given
        val npc = npc(id = "the-guy")
        // When
        npcRepository.save(npc)
        // Then
        npcRepository.searchById("the-guy") shouldBe npc
    }

    @Test
    fun `Delete should remove the npc`() {
        // Given
        npcRepository.save(npc(id = "viking"))
        // When
        npcRepository.delete("viking")
        // Then
        npcRepository.searchById("viking").shouldBeNull()
    }

    @Test
    fun `SearchById should return null when the npc is not stored`() {
        // When
        val npc = npcRepository.searchById("unknown")
        // Then
        npc.shouldBeNull()
    }

    @Test
    fun `SearchAll should return every stored npc`() {
        // Given
        val theGuy = npc(id = "the-guy")
        val theGirl = npc(id = "the-girl")
        npcRepository.save(theGuy)
        npcRepository.save(theGirl)
        // When
        val npcs = npcRepository.searchAll()
        // Then
        npcs.shouldContainExactly(theGuy, theGirl)
    }
}
