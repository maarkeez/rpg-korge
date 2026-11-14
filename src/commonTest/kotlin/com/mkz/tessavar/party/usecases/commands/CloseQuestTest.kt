package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CloseQuestTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val closeQuest = CloseQuest(partyRepository, eventBus)

    @Test
    fun `should publish QuestClosed when reading the quest`() {
        // Given
        partyRepository.save(party().openQuest().pullEvents().second)
        // When
        closeQuest()
        // Then
        partyRepository.search()!!.canStep().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.QuestClosed(playerId = "human"))
    }

    @Test
    fun `should publish nothing when not reading the quest`() {
        // Given
        partyRepository.save(party())
        // When
        closeQuest()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
