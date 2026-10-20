package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LeavePartyTest {
    private val partyRepository = InMemoryPartyRepository()
    private val leaveParty = LeaveParty(partyRepository)

    @Test
    fun `should have the next battle unit lead when the leader leaves the party`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        leaveParty("knight-1")
        // Then
        val party = partyRepository.search()!!.toDto()
        party.leaderBattleUnitId shouldBe "knight-2"
        party.battleUnitIds shouldBe listOf("knight-2")
    }

    @Test
    fun `should keep the party when the battle unit is not in it`() {
        // Given
        val storedParty = party(battleUnitIds = listOf("knight-1", "knight-2"))
        partyRepository.save(storedParty)
        // When
        leaveParty("rat-1")
        // Then
        partyRepository.search() shouldBe storedParty
    }

    @Test
    fun `should do nothing when nobody explores`() {
        // When
        leaveParty("knight-1")
        // Then
        partyRepository.search().shouldBeNull()
    }
}
