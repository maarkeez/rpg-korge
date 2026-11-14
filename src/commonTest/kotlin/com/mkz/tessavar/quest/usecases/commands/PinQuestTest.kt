package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.QuestError.QuestNotActive
import com.mkz.tessavar.quest.domain.QuestError.QuestNotFound
import com.mkz.tessavar.quest.domain.QuestEvent.QuestPinned
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class PinQuestTest {
    private val mainQuestId = "defeat-the-goat-wizard"
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val pinQuest = PinQuest(questRepository, PublishQuestProgress(questRepository, eventBus), eventBus)

    private fun Quest.started() = start().pullEvents().second

    private fun Quest.pinned() = pin().pullEvents().second

    private fun pinnedQuests() =
        questRepository
            .searchAll()
            .map { quest -> quest.toDto() }
            .filter { quest -> quest.progress.isPinned }
            .map { quest -> quest.id }

    @Test
    fun `PinQuest should pin the quest and publish QuestPinned when it is active`() {
        // Given
        questRepository.save(mainQuest(id = mainQuestId).started())
        questRepository.save(sideQuest(id = "bats").started())
        // When
        pinQuest(questId = "bats")
        // Then
        pinnedQuests().shouldContainExactly("bats")
        eventBus.shouldHavePublishedEvents(QuestPinned(questId = "bats"))
    }

    @Test
    fun `PinQuest should unpin the other quest when another one is pinned`() {
        // Given
        questRepository.save(mainQuest(id = mainQuestId).started())
        questRepository.save(sideQuest(id = "bats").started().pinned())
        questRepository.save(sideQuest(id = "rats").started())
        // When
        pinQuest(questId = "rats")
        // Then
        pinnedQuests().shouldContainExactly("rats")
    }

    @Test
    fun `PinQuest should clear every pin when it is the pinned quest`() {
        // Given
        questRepository.save(mainQuest(id = mainQuestId).started())
        questRepository.save(sideQuest(id = "bats").started().pinned())
        // When
        pinQuest(questId = "bats")
        // Then
        pinnedQuests().shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(QuestPinned(questId = mainQuestId))
    }

    @Test
    fun `PinQuest should clear every pin when it is the main quest`() {
        // Given
        questRepository.save(mainQuest(id = mainQuestId).started())
        questRepository.save(sideQuest(id = "bats").started().pinned())
        // When
        pinQuest(questId = mainQuestId)
        // Then
        pinnedQuests().shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(QuestPinned(questId = mainQuestId))
    }

    @Test
    fun `PinQuest should keep the pin of the other quest when it fails`() {
        // Given
        questRepository.save(mainQuest(id = mainQuestId).started())
        questRepository.save(sideQuest(id = "bats").started().pinned())
        questRepository.save(sideQuest(id = "rats"))
        // When
        shouldThrow<QuestNotActive> { pinQuest(questId = "rats") }
        // Then
        pinnedQuests().shouldContainExactly("bats")
    }

    @Test
    fun `PinQuest should fail when the quest does not exist`() {
        shouldThrow<QuestNotFound> { pinQuest(questId = "missing") }
    }
}
