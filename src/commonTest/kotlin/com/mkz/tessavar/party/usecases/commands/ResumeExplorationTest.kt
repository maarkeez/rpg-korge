package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResumeExplorationTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val resumeExploration = ResumeExploration(partyRepository, eventBus)

    @Test
    fun `should explore again when the exploration is interrupted`() {
        // Given
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")).interrupt().pullEvents().second)
        // When
        resumeExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1")))
    }

    @Test
    fun `should not explore again when the game is over`() {
        // Given
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1"))
                .interrupt()
                .leave("knight-1")
                .pullEvents()
                .second,
        )
        // When
        resumeExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isOver
            .shouldBeTrue()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not resume again when the party is already exploring`() {
        // Given
        partyRepository.save(party())
        // When
        resumeExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not resume when nobody explores`() {
        // When
        resumeExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
