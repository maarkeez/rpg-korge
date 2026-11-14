package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class FinishTalkTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val finishTalk = FinishTalk(partyRepository, eventBus)

    @Test
    fun `should explore again when the party is talking`() {
        // Given
        partyRepository.save(party().talk("the-guy").pullEvents().second)
        // When
        finishTalk()
        // Then
        val explorationDto = partyRepository.search()!!.toDto()
        explorationDto.talkingNpcId.shouldBeNull()
        explorationDto.isExploring.shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.NpcTalkFinished(playerId = "human", npcId = "the-guy"))
    }

    @Test
    fun `should do nothing when the party is not talking`() {
        // Given
        partyRepository.save(party())
        // When
        finishTalk()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
