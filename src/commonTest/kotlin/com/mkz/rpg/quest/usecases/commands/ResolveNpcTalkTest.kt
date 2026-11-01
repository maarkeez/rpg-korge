package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.talkToNpc
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResolveNpcTalkTest {
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val resolveNpcTalk = ResolveNpcTalk(questRepository, PublishQuestProgress(questRepository, eventBus))

    @Test
    fun `ResolveNpcTalk should complete the task when the talk with its npc finishes`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talkToNpc(npcId = "elder"))).start().pullEvents().second)
        // When
        resolveNpcTalk(npcId = "elder")
        // Then
        questRepository
            .searchById("talk-to-the-elder")!!
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
        eventBus.shouldHavePublishedEvents(TaskCompleted(questId = "talk-to-the-elder", taskIndex = 0), QuestCompleted(questId = "talk-to-the-elder"))
    }

    @Test
    fun `ResolveNpcTalk should not complete the task when the talk with another npc finishes`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talkToNpc(npcId = "elder"))).start().pullEvents().second)
        // When
        resolveNpcTalk(npcId = "merchant")
        // Then
        questRepository
            .searchById("talk-to-the-elder")!!
            .toDto()
            .progress.status shouldBe StatusDto.ACTIVE
        eventBus.shouldNotHavePublishedEvents()
    }
}
