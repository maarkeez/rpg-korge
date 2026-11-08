package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.talk
import com.mkz.rpg.quest.usecases.commands.ResolveNpcTalk
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnNpcTalkFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val questRepository = InMemoryQuestRepository()
    private val onNpcTalkFinished = OnNpcTalkFinished(ResolveNpcTalk(questRepository, PublishQuestProgress(questRepository, eventBus)), eventBus)

    @Test
    fun `should resolve the talk when the party finishes talking to an npc`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talk(npcId = "elder"))).start().pullEvents().second)
        eventBus.publish(NpcTalkFinished(playerId = "human", npcId = "elder"))
        // When
        eventBus.dispatch()
        // Then
        questRepository
            .searchById("talk-to-the-elder")!!
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
    }
}
