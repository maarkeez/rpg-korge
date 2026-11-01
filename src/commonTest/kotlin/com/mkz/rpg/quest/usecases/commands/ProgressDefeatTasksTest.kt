package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestEvent.TaskProgressed
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ProgressDefeatTasksTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val progressDefeatTasks = ProgressDefeatTasks(questRepository, queries.searchParty, PublishQuestProgress(questRepository, eventBus))

    private fun startedQuest(id: String) = mainQuest(id = id, tasks = listOf(defeatUnits(unitId = "goat-wizard"))).start().pullEvents().second

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
            TaskProgressed(questId = "defeat-the-goat-wizard", taskIndex = 0, progress = 1, count = 1),
            TaskCompleted(questId = "defeat-the-goat-wizard", taskIndex = 0),
            QuestCompleted(questId = "defeat-the-goat-wizard"),
        )
    }

    @Test
    fun `ProgressDefeatTasks should not count a defeat when the unit is of the party's player`() {
        // Given
        queries.partyRepository.save(party(playerId = "human"))
        questRepository.save(startedQuest("defeat-the-goat-wizard"))
        // When
        progressDefeatTasks(playerId = "human", unitId = "goat-wizard")
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.currentTaskProgress shouldBe 0
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
        questRepository.save(mainQuest(id = "main", tasks = listOf(defeatUnits(unitId = "rat", count = 2))).start().pullEvents().second)
        questRepository.save(sideQuest(id = "side", tasks = listOf(defeatUnits(unitId = "rat", count = 2))).start().pullEvents().second)
        // When
        progressDefeatTasks(playerId = "cpu", unitId = "rat")
        // Then
        eventBus.shouldHavePublishedEvents(
            TaskProgressed(questId = "main", taskIndex = 0, progress = 1, count = 2),
            TaskProgressed(questId = "side", taskIndex = 0, progress = 1, count = 2),
        )
    }
}
