package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartTalkTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val startTalk = StartTalk(partyRepository, eventBus)

    private fun talkingNpcId() = partyRepository.search()!!.toDto().talkingNpcId

    @Test
    fun `should talk to the npc when the party can walk`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        startTalk(npcId = "the-guy")
        // Then
        talkingNpcId() shouldBe "the-guy"
        eventBus.shouldHavePublishedEvents(PartyEvent.NpcTalkStarted(playerId = "human", npcId = "the-guy"))
    }

    @Test
    fun `should not talk while the party is walking`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")).walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 1))))
        // When
        startTalk(npcId = "the-guy")
        // Then
        talkingNpcId().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not talk while battling`() {
        // Given
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1", "knight-2"))
                .pause()
                .pullEvents()
                .second,
        )
        // When
        startTalk(npcId = "the-guy")
        // Then
        talkingNpcId().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
