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

class OpenQuestTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val openQuest = OpenQuest(partyRepository, eventBus)

    private fun isReadingQuest() = partyRepository.search()!!.toDto().isReadingQuest

    @Test
    fun `should publish QuestOpened when the party can step`() {
        // Given
        partyRepository.save(party())
        // When
        openQuest()
        // Then
        isReadingQuest().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.QuestOpened(playerId = "human"))
    }

    @Test
    fun `should publish nothing when the party can not step`() {
        // Given
        partyRepository.save(party().interrupt().pullEvents().second)
        // When
        openQuest()
        // Then
        isReadingQuest().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
