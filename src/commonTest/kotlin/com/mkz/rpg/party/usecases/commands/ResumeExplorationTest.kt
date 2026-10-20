package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResumeExplorationTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val resumeExploration = ResumeExploration(partyRepository, eventBus)

    @Test
    fun `should explore again when the exploration is paused`() {
        // Given
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")).pause().pullEvents().second)
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
