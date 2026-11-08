package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.visit
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class ProgressVisitTasksTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val progressVisitTasks = ProgressVisitTasks(questRepository, queries.searchParty, PublishQuestProgress(questRepository, eventBus))

    private fun quest(vararg tasks: Quest.Dto.TaskDto) = mainQuest(id = "cross-the-bridge", tasks = tasks.toList()).start().pullEvents().second

    private fun progress() = questRepository.searchById("cross-the-bridge")!!.toDto().progress

    // The first knight leads, the second one follows.
    @BeforeTest
    fun setUp() {
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
    }

    @Test
    fun `ProgressVisitTasks should complete the task when a follower walks onto any tile of the group`() {
        // Given
        questRepository.save(quest(visit(17 to 11, 18 to 11)))
        // When
        progressVisitTasks(battleUnitId = "knight-2", path = listOf(PositionDto(row = 18, column = 11)))
        // Then
        progress().status shouldBe StatusDto.COMPLETED
    }

    @Test
    fun `ProgressVisitTasks should complete the task when a walk crosses a tile on its way`() {
        // Given
        questRepository.save(quest(visit(17 to 11)))
        // When
        progressVisitTasks(battleUnitId = "knight-1", path = listOf(PositionDto(row = 17, column = 11), PositionDto(row = 17, column = 12)))
        // Then
        progress().status shouldBe StatusDto.COMPLETED
    }

    @Test
    fun `ProgressVisitTasks should not count a battle unit out of the party`() {
        // Given
        questRepository.save(quest(visit(17 to 11)))
        // When
        progressVisitTasks(battleUnitId = "bee-1", path = listOf(PositionDto(row = 17, column = 11)))
        // Then
        progress().status shouldBe StatusDto.ACTIVE
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `ProgressVisitTasks should complete at most one task per event when the next task asks for the same tile`() {
        // Given
        questRepository.save(quest(visit(17 to 11), visit(17 to 11)))
        // When
        progressVisitTasks(battleUnitId = "knight-1", path = listOf(PositionDto(row = 17, column = 11)))
        // Then
        progress().status shouldBe StatusDto.ACTIVE
        progress().currentTaskIndex shouldBe 1
        eventBus.shouldHavePublishedEvents(TaskCompleted(questId = "cross-the-bridge", taskIndex = 0))
    }
}
