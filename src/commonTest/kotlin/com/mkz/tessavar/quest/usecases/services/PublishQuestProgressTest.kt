package com.mkz.tessavar.quest.usecases.services

import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.QuestEvent.QuestStarted
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PublishQuestProgressTest {
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val publishQuestProgress = PublishQuestProgress(questRepository, eventBus)

    @Test
    fun `PublishQuestProgress should save the quest with no pending events and publish them when a fact changed it`() {
        // Given
        val quest = mainQuest(id = "defeat-the-goat-wizard").start()
        // When
        publishQuestProgress(quest)
        // Then
        val savedQuest = questRepository.searchById("defeat-the-goat-wizard")!!
        savedQuest.toDto() shouldBe quest.toDto()
        savedQuest.pullEvents().first.shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "defeat-the-goat-wizard"))
    }

    @Test
    fun `PublishQuestProgress should leave the quest as it is when the fact did not change it`() {
        // When
        publishQuestProgress(mainQuest(id = "defeat-the-goat-wizard"))
        // Then
        questRepository.searchAll().shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }
}
