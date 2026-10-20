package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
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
