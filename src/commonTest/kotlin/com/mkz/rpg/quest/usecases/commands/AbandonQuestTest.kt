package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.QuestError.MainQuestCanNotBeAbandoned
import com.mkz.rpg.quest.domain.QuestError.QuestNotFound
import com.mkz.rpg.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AbandonQuestTest {
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val abandonQuest = AbandonQuest(questRepository, PublishQuestProgress(questRepository, eventBus))

    @Test
    fun `AbandonQuest should allow its giver to start it again when it is not repeatable`() {
        // Given
        val startedQuest =
            sideQuest(id = "clear-the-rats", giverNpcId = "farmer", tasks = listOf(defeatUnits(unitId = "rat", count = 5)))
                .start()
                .defeated("rat")
                .pullEvents()
                .second
        questRepository.save(startedQuest)
        // When
        abandonQuest(questId = "clear-the-rats")
        // Then
        val abandonedQuest = questRepository.searchById("clear-the-rats")!!
        abandonedQuest.toDto().progress shouldBe ProgressDto.notStarted("clear-the-rats")
        abandonedQuest.canBeStartedBy("farmer") shouldBe true
        eventBus.shouldHavePublishedEvents(QuestAbandoned(questId = "clear-the-rats"))
    }

    @Test
    fun `AbandonQuest should fail when the quest does not exist`() {
        shouldThrow<QuestNotFound> { abandonQuest(questId = "missing") }
    }

    @Test
    fun `AbandonQuest should fail when it is the main quest`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard").start().pullEvents().second)
        // When / Then
        shouldThrow<MainQuestCanNotBeAbandoned> { abandonQuest(questId = "defeat-the-goat-wizard") }
    }
}
