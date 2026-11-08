package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
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
