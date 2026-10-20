package com.mkz.rpg.party.usecases.queries

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchPartyTest {
    private val partyRepository = InMemoryPartyRepository()
    private val searchParty = SearchParty(partyRepository)

    @Test
    fun `should return the party when it is exploring`() {
        // Given
        val party = party()
        partyRepository.save(party)
        // When
        val result = searchParty()
        // Then
        result shouldBe party.toDto()
    }

    @Test
    fun `should return null when nobody explores`() {
        // When
        val result = searchParty()
        // Then
        result.shouldBeNull()
    }
}
