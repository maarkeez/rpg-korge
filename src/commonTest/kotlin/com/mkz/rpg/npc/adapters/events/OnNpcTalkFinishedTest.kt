package com.mkz.rpg.npc.adapters.events

import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.npc.usecases.commands.RecruitNpc
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
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
