package com.mkz.rpg.quest.domain

import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.quest.domain.QuestError.EmptyQuestReward
import com.mkz.rpg.quest.domain.QuestError.EmptyTaskTiles
import com.mkz.rpg.quest.domain.QuestError.InvalidTask
import com.mkz.rpg.quest.domain.QuestError.MainQuestCanNotBeAbandoned
import com.mkz.rpg.quest.domain.QuestError.MainQuestRepeatable
import com.mkz.rpg.quest.domain.QuestError.MainQuestWithGiver
import com.mkz.rpg.quest.domain.QuestError.NegativeQuestRewardGold
import com.mkz.rpg.quest.domain.QuestError.QuestNotActive
import com.mkz.rpg.quest.domain.QuestError.QuestWithoutTasks
import com.mkz.rpg.quest.domain.QuestError.SideQuestWithoutGiver
import com.mkz.rpg.quest.domain.QuestError.TaskCountBelowOne
import com.mkz.rpg.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestPinned
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestEvent.TaskProgressed
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reachTiles
import com.mkz.rpg.quest.domain.QuestMother.reward
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.domain.QuestMother.talkToNpc
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuestTest {
    private fun started(tasks: List<TaskDto>) = mainQuest(id = "a-quest", tasks = tasks).start().pullEvents().second

    @Test
    fun `Quest should start not started on its first task when it is created`() {
        // When
        val quest = mainQuest(id = "defeat-the-goat-wizard")
        // Then
        quest.toDto().progress shouldBe ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskProgress = 0, isPinned = false)
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
    fun `Quest should fail when a task has no details of its type`() {
        // Given
        val task = TaskDto(type = TaskDto.TypeDto.TALK_TO_NPC, defeatUnits = null, talkToNpc = null, reachTiles = null)
        // When / Then
        shouldThrow<InvalidTask> { mainQuest(tasks = listOf(task)) }
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
        startedQuest.toDto().progress shouldBe ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskProgress = 0, isPinned = false)
        startedQuest.pullEvents().first.shouldContainExactly(QuestStarted(questId = "defeat-the-goat-wizard"))
    }

    @Test
    fun `Quest should move on to the next task when the current one is done`() {
        // Given
        val quest = started(tasks = listOf(reachTiles(1 to 1), defeatUnits(unitId = "rat")))
        // When
        val updatedQuest = quest.reached(listOf(PositionDto(row = 1, column = 1)))
        // Then
        updatedQuest.toDto().progress shouldBe ProgressDto(questId = "a-quest", status = StatusDto.ACTIVE, currentTaskIndex = 1, currentTaskProgress = 0, isPinned = false)
        updatedQuest.pullEvents().first.shouldContainExactly(TaskCompleted(questId = "a-quest", taskIndex = 0))
    }

    @Test
    fun `Quest should not count an event when its task is not the current one`() {
        // Given
        val quest = started(tasks = listOf(reachTiles(1 to 1), defeatUnits(unitId = "rat")))
        // When
        val updatedQuest = quest.defeated("rat")
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should not count an event when the current task asks for something else`() {
        // Given
        val quest = started(tasks = listOf(defeatUnits(unitId = "rat")))
        // When
        val updatedQuest = quest.defeated("bat").reached(listOf(PositionDto(row = 1, column = 1)))
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should not count an event when it has not started`() {
        // Given
        val quest = mainQuest(tasks = listOf(defeatUnits(unitId = "rat")))
        // When
        val updatedQuest = quest.defeated("rat")
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should complete only once the defeats reach the count`() {
        // Given
        val quest = started(tasks = listOf(defeatUnits(unitId = "rat", count = 2)))
        // When
        val onceDefeated = quest.defeated("rat").pullEvents()
        val twiceDefeated = onceDefeated.second.defeated("rat").pullEvents()
        // Then
        onceDefeated.second.toDto().progress shouldBe ProgressDto(questId = "a-quest", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskProgress = 1, isPinned = false)
        onceDefeated.first.shouldContainExactly(TaskProgressed(questId = "a-quest", taskIndex = 0, progress = 1, count = 2))
        twiceDefeated.second
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
        twiceDefeated.first.shouldContainExactly(
            TaskProgressed(questId = "a-quest", taskIndex = 0, progress = 2, count = 2),
            TaskCompleted(questId = "a-quest", taskIndex = 0),
            QuestCompleted(questId = "a-quest"),
        )
    }

    @Test
    fun `Quest should complete when the last task is done`() {
        // Given
        val quest = started(tasks = listOf(reachTiles(1 to 1)))
        // When
        val updatedQuest = quest.reached(listOf(PositionDto(row = 0, column = 0), PositionDto(row = 1, column = 1)))
        // Then
        updatedQuest.toDto().progress.status shouldBe StatusDto.COMPLETED
        updatedQuest.pullEvents().first.shouldContainExactly(TaskCompleted(questId = "a-quest", taskIndex = 0), QuestCompleted(questId = "a-quest"))
    }

    @Test
    fun `Quest should complete the task when the talk is with its npc`() {
        // Given
        val quest = started(tasks = listOf(talkToNpc(npcId = "elder")))
        // When
        val updatedQuest = quest.talkedTo("elder")
        // Then
        updatedQuest.toDto().progress.status shouldBe StatusDto.COMPLETED
    }

    @Test
    fun `Quest should not complete the task when the talk is with another npc`() {
        // Given
        val quest = started(tasks = listOf(talkToNpc(npcId = "elder")))
        // When
        val updatedQuest = quest.talkedTo("merchant")
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should not count an event when it is completed`() {
        // Given
        val completedQuest = started(tasks = listOf(defeatUnits(unitId = "rat"))).defeated("rat").pullEvents().second
        // When
        val updatedQuest = completedQuest.defeated("rat")
        // Then
        updatedQuest shouldBe completedQuest
    }

    @Test
    fun `Quest should be startable by its giver when it is not started`() {
        // Given
        val quest = sideQuest(giverNpcId = "farmer")
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe true
    }

    @Test
    fun `Quest should not be startable by another npc`() {
        // Given
        val quest = sideQuest(giverNpcId = "farmer")
        // When / Then
        quest.canBeStartedBy("blacksmith") shouldBe false
    }

    @Test
    fun `Quest should not be startable by its giver when it is active`() {
        // Given
        val quest = sideQuest(giverNpcId = "farmer", repeatable = true).start()
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe false
    }

    @Test
    fun `Quest should be startable by its giver when it is completed and repeatable`() {
        // Given
        val quest = sideQuest(giverNpcId = "farmer", repeatable = true, tasks = listOf(defeatUnits(unitId = "rat"))).start().defeated("rat")
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe true
    }

    @Test
    fun `Quest should not be startable by its giver when it is completed and not repeatable`() {
        // Given
        val quest = sideQuest(giverNpcId = "farmer", tasks = listOf(defeatUnits(unitId = "rat"))).start().defeated("rat")
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe false
    }

    @Test
    fun `Quest should not be startable by any npc when it is the main quest`() {
        // Given
        val quest = mainQuest()
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe false
    }

    @Test
    fun `Quest should reset its progress when it is abandoned`() {
        // Given
        val quest =
            sideQuest(
                id = "a-quest",
                tasks = listOf(reachTiles(1 to 1), defeatUnits(unitId = "rat", count = 5)),
            ).start().reached(listOf(PositionDto(row = 1, column = 1))).defeated("rat").pullEvents().second
        // When
        val abandonedQuest = quest.abandon()
        // Then
        abandonedQuest.toDto().progress shouldBe ProgressDto.notStarted("a-quest")
        abandonedQuest.pullEvents().first.shouldContainExactly(QuestAbandoned(questId = "a-quest"))
    }

    @Test
    fun `Quest should fail when a main quest is abandoned`() {
        // Given
        val quest = mainQuest().start()
        // When / Then
        shouldThrow<MainQuestCanNotBeAbandoned> { quest.abandon() }
    }

    @Test
    fun `Quest should fail when it is abandoned and not active`() {
        // Given
        val quest = sideQuest()
        // When / Then
        shouldThrow<QuestNotActive> { quest.abandon() }
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

    @Test
    fun `Pin should mark the quest and publish QuestPinned when it is active`() {
        // When
        val (events, pinned) = started(listOf(defeatUnits())).pin().pullEvents()
        // Then
        pinned.toDto().progress.isPinned shouldBe true
        events.shouldContainExactly(QuestPinned(questId = "a-quest"))
    }

    @Test
    fun `Pin should fail with QuestNotActive when it is not active`() {
        shouldThrow<QuestNotActive> { mainQuest().pin() }
    }

    @Test
    fun `Unpin should clear the pin and publish nothing when it is pinned`() {
        // Given
        val pinned = started(listOf(defeatUnits())).pin().pullEvents().second
        // When
        val (events, unpinned) = pinned.unpin().pullEvents()
        // Then
        unpinned.toDto().progress.isPinned shouldBe false
        events.shouldBeEmpty()
    }

    @Test
    fun `Abandon should unpin the quest when it is pinned`() {
        // Given
        val pinned =
            sideQuest()
                .start()
                .pin()
                .pullEvents()
                .second
        // When
        val abandoned = pinned.abandon()
        // Then
        abandoned.toDto().progress.isPinned shouldBe false
    }

    @Test
    fun `Defeated should unpin the quest when its last task is completed`() {
        // Given
        val pinned = started(listOf(defeatUnits(unitId = "rat", count = 1))).pin().pullEvents().second
        // When
        val completed = pinned.defeated("rat")
        // Then
        completed.toDto().progress.status shouldBe StatusDto.COMPLETED
        completed.toDto().progress.isPinned shouldBe false
    }

    @Test
    fun `Defeated should keep the pin when a task is left`() {
        // Given
        val pinned = started(listOf(defeatUnits(unitId = "rat", count = 1), talkToNpc("farmer"))).pin().pullEvents().second
        // When
        val progressed = pinned.defeated("rat")
        // Then
        progressed.toDto().progress.isPinned shouldBe true
    }
}
