package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.StartTalk
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestStartTalkTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onRequestStartTalk = OnRequestStartTalk(StartTalk(partyRepository, eventBus), eventBus)

    @Test
    fun `should talk to the npc when its talk is requested`() {
        // Given
        partyRepository.save(party())
        eventBus.publish(PartyEvent.RequestStartTalk(npcId = "the-guy"))
        // When
        eventBus.dispatch()
        // Then
        partyRepository.search()!!.toDto().talkingNpcId shouldBe "the-guy"
    }
}
