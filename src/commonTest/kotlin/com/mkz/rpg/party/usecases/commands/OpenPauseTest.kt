package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OpenPauseTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val openPause = OpenPause(partyRepository, eventBus)

    private fun isPaused() = partyRepository.search()!!.toDto().isPaused

    @Test
    fun `should pause when the party can walk`() {
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
        partyRepository.save(party().pause().pullEvents().second)
        // When
        openPause()
        // Then
        isPaused().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
