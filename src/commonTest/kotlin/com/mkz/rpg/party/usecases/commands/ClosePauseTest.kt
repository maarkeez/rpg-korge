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

class ClosePauseTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val closePause = ClosePause(partyRepository, eventBus)

    private fun canWalk() = partyRepository.search()!!.canWalk()

    @Test
    fun `should explore again when the game is paused`() {
        // Given
        partyRepository.save(party().openPause().pullEvents().second)
        // When
        closePause()
        // Then
        canWalk().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.PauseClosed(playerId = "human"))
    }

    @Test
    fun `should do nothing when the game is not paused`() {
        // Given
        partyRepository.save(party().pause().pullEvents().second)
        // When
        closePause()
        // Then
        canWalk().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
