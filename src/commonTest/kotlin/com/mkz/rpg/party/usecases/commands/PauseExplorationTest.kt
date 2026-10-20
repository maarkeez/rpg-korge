package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class PauseExplorationTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val pauseExploration = PauseExploration(partyRepository, eventBus)

    @Test
    fun `should stop exploring when the party is exploring`() {
        // Given
        partyRepository.save(party(playerId = "human"))
        // When
        pauseExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeFalse()
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationPaused(playerId = "human"))
    }

    @Test
    fun `should stop talking when a battle starts while the party is talking`() {
        // Given
        partyRepository.save(party(playerId = "human").talk("the-guy").pullEvents().second)
        // When
        pauseExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .talkingNpcId
            .shouldBeNull()
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationPaused(playerId = "human"))
    }

    @Test
    fun `should not pause again when the exploration is already paused`() {
        // Given
        partyRepository.save(party().pause().pullEvents().second)
        // When
        pauseExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not pause when nobody explores`() {
        // When
        pauseExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
