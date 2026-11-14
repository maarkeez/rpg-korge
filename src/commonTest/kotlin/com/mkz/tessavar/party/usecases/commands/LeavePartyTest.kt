package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LeavePartyTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val leaveParty = LeaveParty(partyRepository, eventBus)

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
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should publish the game over and request the battle to finish when the last member leaves the party`() {
        // Given
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        leaveParty("knight-1")
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isOver
            .shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.GameOver(playerId = "human"), BattleEvent.RequestFinishBattle)
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
