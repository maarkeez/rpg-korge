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

class InterruptExplorationTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val interruptExploration = InterruptExploration(partyRepository, eventBus)

    @Test
    fun `should stop exploring when the party is exploring`() {
        // Given
        partyRepository.save(party(playerId = "human"))
        // When
        interruptExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeFalse()
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationInterrupted(playerId = "human"))
    }

    @Test
    fun `should stop talking when a battle starts while the party is talking`() {
        // Given
        partyRepository.save(party(playerId = "human").talk("the-guy").pullEvents().second)
        // When
        interruptExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .talkingNpcId
            .shouldBeNull()
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationInterrupted(playerId = "human"))
    }

    @Test
    fun `should not interrupt again when the exploration is already interrupted`() {
        // Given
        partyRepository.save(party().interrupt().pullEvents().second)
        // When
        interruptExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not interrupt when nobody explores`() {
        // When
        interruptExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should close the formation when a battle starts while the party is in formation`() {
        // Given
        partyRepository.save(party(playerId = "human").openFormation().pullEvents().second)
        // When
        interruptExploration()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isInFormation
            .shouldBeFalse()
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationInterrupted(playerId = "human"))
    }
}
