package com.mkz.tessavar.npc.adapters.events

import com.mkz.tessavar.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.tessavar.npc.domain.NpcMother.npc
import com.mkz.tessavar.npc.usecases.commands.RecruitNpc
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class OnNpcTalkFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val npcRepository = InMemoryNpcRepository()
    private val onNpcTalkFinished = OnNpcTalkFinished(RecruitNpc(npcRepository, eventBus), eventBus)

    @Test
    fun `should recruit the npc talked to when the talk is finished`() {
        // Given
        npcRepository.save(npc(id = "viking", unitId = "viking"))
        eventBus.publish(PartyEvent.NpcTalkFinished(playerId = "human", npcId = "viking"))
        // When
        eventBus.dispatch()
        // Then
        npcRepository.searchById("viking").shouldBeNull()
    }
}
