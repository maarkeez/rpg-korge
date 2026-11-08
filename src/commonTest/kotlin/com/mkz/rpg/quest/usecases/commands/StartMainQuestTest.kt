package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartMainQuestTest {
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val startMainQuest = StartMainQuest(questRepository, eventBus)

    @Test
    fun `StartMainQuest should start the main quest and no side quest`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard"))
        questRepository.save(sideQuest(id = "help-the-farmer"))
        // When
        startMainQuest()
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.status shouldBe StatusDto.ACTIVE
        questRepository
            .searchById("help-the-farmer")!!
            .toDto()
            .progress.status shouldBe StatusDto.NOT_STARTED
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "defeat-the-goat-wizard"))
    }

    @Test
    fun `StartMainQuest should fail when the quests are not created`() {
        shouldThrow<MainQuestNotUnique> { startMainQuest() }
    }
}
