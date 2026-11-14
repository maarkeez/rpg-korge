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

class OpenPauseTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val openPause = OpenPause(partyRepository, eventBus)

    private fun isPaused() = partyRepository.search()!!.toDto().isPaused

    @Test
    fun `should pause when the party can step`() {
        // Given
        partyRepository.save(party())
        // When
        openPause()
        // Then
        isPaused().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.PauseOpened(playerId = "human"))
    }

    @Test
    fun `should not pause while battling`() {
        // Given
        partyRepository.save(party().interrupt().pullEvents().second)
        // When
        openPause()
        // Then
        isPaused().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
