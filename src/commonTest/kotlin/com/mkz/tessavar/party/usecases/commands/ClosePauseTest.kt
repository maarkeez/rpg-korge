package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ClosePauseTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val closePause = ClosePause(partyRepository, eventBus)

    private fun canStep() = partyRepository.search()!!.canStep()

    @Test
    fun `should explore again when the game is paused`() {
        // Given
        partyRepository.save(party().openPause().pullEvents().second)
        // When
        closePause()
        // Then
        canStep().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.PauseClosed(playerId = "human"))
    }

    @Test
    fun `should do nothing when the game is not paused`() {
        // Given
        partyRepository.save(party().interrupt().pullEvents().second)
        // When
        closePause()
        // Then
        canStep().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
