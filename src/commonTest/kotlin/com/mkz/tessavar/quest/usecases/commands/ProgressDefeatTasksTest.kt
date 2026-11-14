package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestEvent.DefeatCounted
import com.mkz.tessavar.quest.domain.QuestEvent.QuestCompleted
import com.mkz.tessavar.quest.domain.QuestEvent.TaskCompleted
import com.mkz.tessavar.quest.domain.QuestMother.defeat
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ProgressDefeatTasksTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val progressDefeatTasks = ProgressDefeatTasks(questRepository, queries.searchParty, PublishQuestProgress(questRepository, eventBus))

    private fun startedQuest(id: String) = mainQuest(id = id, tasks = listOf(defeat(unitId = "goat-wizard"))).start().pullEvents().second

    @Test
    fun `ProgressDefeatTasks should count a defeat when the unit is of another player`() {
        // Given
        queries.partyRepository.save(party(playerId = "human"))
        questRepository.save(startedQuest("defeat-the-goat-wizard"))
        // When
        progressDefeatTasks(playerId = "cpu", unitId = "goat-wizard")
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
        eventBus.shouldHavePublishedEvents(
            DefeatCounted(questId = "defeat-the-goat-wizard", taskIndex = 0, defeats = 1, requiredDefeats = 1),
            TaskCompleted(questId = "defeat-the-goat-wizard", taskIndex = 0),
            QuestCompleted(questId = "defeat-the-goat-wizard"),
        )
    }

    @Test
    fun `ProgressDefeatTasks should not count a defeat when the unit is of the human player`() {
        // Given
        queries.partyRepository.save(party(playerId = "human"))
        questRepository.save(startedQuest("defeat-the-goat-wizard"))
        // When
        progressDefeatTasks(playerId = "human", unitId = "goat-wizard")
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.currentTaskDefeats shouldBe 0
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `ProgressDefeatTasks should not count a defeat when there is no party`() {
        // Given
        questRepository.save(startedQuest("defeat-the-goat-wizard"))
        // When
        progressDefeatTasks(playerId = "cpu", unitId = "goat-wizard")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `ProgressDefeatTasks should progress every active quest when they ask for the same unit`() {
        // Given
        queries.partyRepository.save(party(playerId = "human"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(defeat(unitId = "rat", requiredDefeats = 2))).start().pullEvents().second)
        questRepository.save(sideQuest(id = "side", tasks = listOf(defeat(unitId = "rat", requiredDefeats = 2))).start().pullEvents().second)
        // When
        progressDefeatTasks(playerId = "cpu", unitId = "rat")
        // Then
        eventBus.shouldHavePublishedEvents(
            DefeatCounted(questId = "main", taskIndex = 0, defeats = 1, requiredDefeats = 2),
            DefeatCounted(questId = "side", taskIndex = 0, defeats = 1, requiredDefeats = 2),
        )
    }
}
