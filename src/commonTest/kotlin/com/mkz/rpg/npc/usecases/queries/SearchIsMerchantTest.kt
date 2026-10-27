package com.mkz.rpg.npc.usecases.queries

import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class SearchIsMerchantTest {
    private val npcRepository = InMemoryNpcRepository()
    private val searchIsMerchant = SearchIsMerchant(npcRepository)

    @Test
    fun `should return true when the npc has a stock`() {
        // Given
        npcRepository.save(npc(id = "merchant", stock = listOf("hammer")))
        // When
        val isMerchant = searchIsMerchant("merchant")
        // Then
        isMerchant.shouldBeTrue()
    }

    @Test
    fun `should return false when the npc has no stock`() {
        // Given
        npcRepository.save(npc(id = "the-guy", stock = emptyList()))
        // When
        val isMerchant = searchIsMerchant("the-guy")
        // Then
        isMerchant.shouldBeFalse()
    }

    @Test
    fun `should return false when the npc does not exist`() {
        // When
        val isMerchant = searchIsMerchant("unknown")
        // Then
        isMerchant.shouldBeFalse()
    }
}
