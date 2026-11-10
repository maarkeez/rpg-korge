package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestMother.defeat
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.domain.QuestMother.talk
import com.mkz.rpg.quest.usecases.queries.SearchQuestLine
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResolveNpcTalkTest {
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val resolveNpcTalk = ResolveNpcTalk(SearchQuestLine(questRepository), questRepository, PublishQuestProgress(questRepository, eventBus))

    private fun status(questId: String) =
        questRepository
            .searchById(questId)!!
            .toDto()
            .progress.status

    @Test
    fun `ResolveNpcTalk should complete the task when the talk with its npc finishes`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talk(npcId = "elder"))).start().pullEvents().second)
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
    fun `ResolveNpcTalk should start only the first side quest by id when its quest giver has two to start`() {
        // Given
        questRepository.save(sideQuest(id = "find-the-cow", questGiverNpcId = "farmer"))
        questRepository.save(sideQuest(id = "clear-the-rats", questGiverNpcId = "farmer"))
        questRepository.save(sideQuest(id = "forge-a-sword", questGiverNpcId = "blacksmith"))
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        status("clear-the-rats") shouldBe StatusDto.ACTIVE
        status("find-the-cow") shouldBe StatusDto.NOT_STARTED
        status("forge-a-sword") shouldBe StatusDto.NOT_STARTED
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "clear-the-rats"))
    }

    @Test
    fun `ResolveNpcTalk should start the other side quest when a later talk finishes`() {
        // Given
        questRepository.save(sideQuest(id = "clear-the-rats", questGiverNpcId = "farmer").start().pullEvents().second)
        questRepository.save(sideQuest(id = "find-the-cow", questGiverNpcId = "farmer"))
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        status("clear-the-rats") shouldBe StatusDto.ACTIVE
        status("find-the-cow") shouldBe StatusDto.ACTIVE
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "find-the-cow"))
    }

    @Test
    fun `ResolveNpcTalk should do the talk task and start no quest when the npc has both`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talk(npcId = "elder"), defeat())).start().pullEvents().second)
        questRepository.save(sideQuest(id = "errand", questGiverNpcId = "elder"))
        // When
        resolveNpcTalk(npcId = "elder")
        // Then
        status("errand") shouldBe StatusDto.NOT_STARTED
        eventBus.shouldHavePublishedEvents(TaskCompleted(questId = "talk-to-the-elder", taskIndex = 0))
    }

    @Test
    fun `ResolveNpcTalk should do only the talk task of the main quest when two quests ask to talk to the npc`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talk(npcId = "elder"), defeat())).start().pullEvents().second)
        questRepository.save(sideQuest(id = "ask-the-elder", questGiverNpcId = "farmer", tasks = listOf(talk(npcId = "elder"), defeat())).start().pullEvents().second)
        // When
        resolveNpcTalk(npcId = "elder")
        // Then
        eventBus.shouldHavePublishedEvents(TaskCompleted(questId = "talk-to-the-elder", taskIndex = 0))
    }

    @Test
    fun `ResolveNpcTalk should change nothing when the quest giver says its reminder line`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer", tasks = listOf(defeat(unitId = "bat"), talk(npcId = "farmer"))).start().pullEvents().second)
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        status("bats") shouldBe StatusDto.ACTIVE
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `ResolveNpcTalk should not complete the first task of a quest when the same talk starts it`() {
        // Given
        questRepository.save(sideQuest(id = "talk-again", questGiverNpcId = "farmer", tasks = listOf(talk(npcId = "farmer"))))
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        status("talk-again") shouldBe StatusDto.ACTIVE
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "talk-again"))
    }

    @Test
    fun `ResolveNpcTalk should not restart a repeatable quest when the same talk completes it`() {
        // Given
        questRepository.save(sideQuest(id = "talk-again", questGiverNpcId = "farmer", repeatable = true, tasks = listOf(talk(npcId = "farmer"))).start().pullEvents().second)
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        status("talk-again") shouldBe StatusDto.COMPLETED
        eventBus.shouldHavePublishedEvents(TaskCompleted(questId = "talk-again", taskIndex = 0), QuestCompleted(questId = "talk-again"))
    }

    @Test
    fun `ResolveNpcTalk should restart a repeatable quest when a later talk finishes`() {
        // Given
        questRepository.save(
            sideQuest(id = "talk-again", questGiverNpcId = "farmer", repeatable = true, tasks = listOf(talk(npcId = "farmer")))
                .start()
                .talkedTo("farmer")
                .pullEvents()
                .second,
        )
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        questRepository.searchById("talk-again")!!.toDto().progress shouldBe
            ProgressDto(questId = "talk-again", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false)
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "talk-again"))
    }

    @Test
    fun `ResolveNpcTalk should not start a quest again when it is completed and not repeatable`() {
        // Given
        questRepository.save(
            sideQuest(id = "talk-again", questGiverNpcId = "farmer", tasks = listOf(talk(npcId = "farmer")))
                .start()
                .talkedTo("farmer")
                .pullEvents()
                .second,
        )
        // When
        resolveNpcTalk(npcId = "farmer")
        // Then
        status("talk-again") shouldBe StatusDto.COMPLETED
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `ResolveNpcTalk should not complete the task when the talk with another npc finishes`() {
        // Given
        questRepository.save(mainQuest(id = "talk-to-the-elder", tasks = listOf(talk(npcId = "elder"))).start().pullEvents().second)
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
