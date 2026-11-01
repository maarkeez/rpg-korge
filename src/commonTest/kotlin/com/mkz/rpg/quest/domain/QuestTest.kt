package com.mkz.rpg.quest.domain

import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestError.EmptyQuestReward
import com.mkz.rpg.quest.domain.QuestError.EmptyTaskTiles
import com.mkz.rpg.quest.domain.QuestError.MainQuestRepeatable
import com.mkz.rpg.quest.domain.QuestError.MainQuestWithGiver
import com.mkz.rpg.quest.domain.QuestError.NegativeQuestRewardGold
import com.mkz.rpg.quest.domain.QuestError.QuestWithoutTasks
import com.mkz.rpg.quest.domain.QuestError.SideQuestWithoutGiver
import com.mkz.rpg.quest.domain.QuestError.TaskCountBelowOne
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reachTiles
import com.mkz.rpg.quest.domain.QuestMother.reward
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuestTest {
    @Test
    fun `Quest should start not started on its first task when it is created`() {
        // When
        val quest = mainQuest(id = "defeat-the-goat-wizard")
        // Then
        quest.toDto().progress shouldBe ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskProgress = 0)
        quest.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `Quest should fail when it has no tasks`() {
        shouldThrow<QuestWithoutTasks> { mainQuest(tasks = emptyList()) }
    }

    @Test
    fun `Quest should fail when its reward is empty`() {
        shouldThrow<EmptyQuestReward> { mainQuest(reward = reward(gold = 0, lootableItemIds = emptyList())) }
    }

    @Test
    fun `Quest should fail when its reward gold is negative`() {
        shouldThrow<NegativeQuestRewardGold> { mainQuest(reward = reward(gold = -1, lootableItemIds = listOf("hammer"))) }
    }

    @Test
    fun `Quest should fail when a task has no tiles`() {
        shouldThrow<EmptyTaskTiles> { mainQuest(tasks = listOf(reachTiles())) }
    }

    @Test
    fun `Quest should fail when a task has a count below one`() {
        shouldThrow<TaskCountBelowOne> { mainQuest(tasks = listOf(defeatUnits(count = 0))) }
    }

    @Test
    fun `Quest should fail when a side quest has no giver`() {
        // Given
        val dto = sideQuest().toDto().copy(giverNpcId = null)
        // When / Then
        shouldThrow<SideQuestWithoutGiver> { Quest.create(dto) }
    }

    @Test
    fun `Quest should fail when a main quest has a giver`() {
        // Given
        val dto = mainQuest().toDto().copy(giverNpcId = "farmer")
        // When / Then
        shouldThrow<MainQuestWithGiver> { Quest.create(dto) }
    }

    @Test
    fun `Quest should fail when a main quest is repeatable`() {
        // Given
        val dto = sideQuest(repeatable = true).toDto().copy(type = QuestTypeDto.MAIN, giverNpcId = null)
        // When / Then
        shouldThrow<MainQuestRepeatable> { Quest.create(dto) }
    }

    @Test
    fun `Quest should be active on its first task and announce it when it starts`() {
        // Given
        val quest = mainQuest(id = "defeat-the-goat-wizard")
        // When
        val startedQuest = quest.start()
        // Then
        startedQuest.toDto().progress shouldBe ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskProgress = 0)
        startedQuest.pullEvents().first.shouldContainExactly(QuestStarted(questId = "defeat-the-goat-wizard"))
    }

    @Test
    fun `PullEvents should clear the pending events when the events are pulled`() {
        // Given
        val quest = mainQuest().start()
        // When
        val (_, clearedQuest) = quest.pullEvents()
        // Then
        clearedQuest.pullEvents().first.shouldBeEmpty()
    }
}
