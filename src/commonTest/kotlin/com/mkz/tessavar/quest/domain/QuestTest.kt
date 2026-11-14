package com.mkz.tessavar.quest.domain

import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.Quest.Dto.ProgressDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestLineDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestLineDto.KindDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.Quest.Dto.TaskDto
import com.mkz.tessavar.quest.domain.QuestError.EmptyQuestReward
import com.mkz.tessavar.quest.domain.QuestError.EmptyVisitTaskTiles
import com.mkz.tessavar.quest.domain.QuestError.InvalidTask
import com.mkz.tessavar.quest.domain.QuestError.MainQuestCanNotBeAbandoned
import com.mkz.tessavar.quest.domain.QuestError.MainQuestRepeatable
import com.mkz.tessavar.quest.domain.QuestError.MainQuestWithQuestGiver
import com.mkz.tessavar.quest.domain.QuestError.MainQuestWithoutStartTiles
import com.mkz.tessavar.quest.domain.QuestError.NegativeQuestRewardGold
import com.mkz.tessavar.quest.domain.QuestError.QuestNotActive
import com.mkz.tessavar.quest.domain.QuestError.QuestWithoutTasks
import com.mkz.tessavar.quest.domain.QuestError.RequiredDefeatsBelowOne
import com.mkz.tessavar.quest.domain.QuestError.SideQuestWithStartTiles
import com.mkz.tessavar.quest.domain.QuestError.SideQuestWithoutQuestGiver
import com.mkz.tessavar.quest.domain.QuestEvent.DefeatCounted
import com.mkz.tessavar.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.tessavar.quest.domain.QuestEvent.QuestCompleted
import com.mkz.tessavar.quest.domain.QuestEvent.QuestPinned
import com.mkz.tessavar.quest.domain.QuestEvent.QuestStarted
import com.mkz.tessavar.quest.domain.QuestEvent.TaskCompleted
import com.mkz.tessavar.quest.domain.QuestMother.defeat
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.reward
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.domain.QuestMother.talk
import com.mkz.tessavar.quest.domain.QuestMother.visit
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
        quest.toDto().progress shouldBe ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.NOT_STARTED, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false)
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
        val task = TaskDto(type = TaskDto.TypeDto.TALK, defeat = null, talk = null, visit = null)
        // When / Then
        shouldThrow<InvalidTask> { mainQuest(tasks = listOf(task)) }
    }

    @Test
    fun `Quest should fail when a task has no tiles`() {
        shouldThrow<EmptyVisitTaskTiles> { mainQuest(tasks = listOf(visit())) }
    }

    @Test
    fun `Quest should fail when a defeat task has fewer than one required defeat`() {
        shouldThrow<RequiredDefeatsBelowOne> { mainQuest(tasks = listOf(defeat(requiredDefeats = 0))) }
    }

    @Test
    fun `Quest should fail when a side quest has no quest giver`() {
        // Given
        val dto = sideQuest().toDto().copy(questGiverNpcId = null)
        // When / Then
        shouldThrow<SideQuestWithoutQuestGiver> { Quest.create(dto) }
    }

    @Test
    fun `Quest should fail when a main quest has a quest giver`() {
        // Given
        val dto = mainQuest().toDto().copy(questGiverNpcId = "farmer")
        // When / Then
        shouldThrow<MainQuestWithQuestGiver> { Quest.create(dto) }
    }

    @Test
    fun `Quest should fail when a main quest is repeatable`() {
        // Given
        val dto = sideQuest(repeatable = true).toDto().copy(type = QuestTypeDto.MAIN, questGiverNpcId = null, startTiles = listOf(PositionDto(row = 14, column = 10)))
        // When / Then
        shouldThrow<MainQuestRepeatable> { Quest.create(dto) }
    }

    @Test
    fun `Quest should fail when a main quest has no start tiles`() {
        // Given
        val dto = mainQuest().toDto().copy(startTiles = emptyList())
        // When / Then
        shouldThrow<MainQuestWithoutStartTiles> { Quest.create(dto) }
    }

    @Test
    fun `Quest should fail when a side quest has start tiles`() {
        // Given
        val dto = sideQuest().toDto().copy(startTiles = listOf(PositionDto(row = 14, column = 10)))
        // When / Then
        shouldThrow<SideQuestWithStartTiles> { Quest.create(dto) }
    }

    @Test
    fun `CanBeStartedOn should be true when a tile is a start tile of a main quest not started`() {
        // Given
        val quest = mainQuest(startTiles = listOf(PositionDto(row = 14, column = 9), PositionDto(row = 14, column = 10)))
        // When / Then
        quest.canBeStartedOn(listOf(PositionDto(row = 13, column = 10), PositionDto(row = 14, column = 10))) shouldBe true
    }

    @Test
    fun `CanBeStartedOn should be false when no tile is a start tile`() {
        // Given
        val quest = mainQuest(startTiles = listOf(PositionDto(row = 14, column = 10)))
        // When / Then
        quest.canBeStartedOn(listOf(PositionDto(row = 15, column = 10))) shouldBe false
        sideQuest().canBeStartedOn(listOf(PositionDto(row = 14, column = 10))) shouldBe false
    }

    @Test
    fun `CanBeStartedOn should be false when the main quest is active or completed`() {
        // Given
        val startTiles = listOf(PositionDto(row = 14, column = 10))
        val activeQuest = mainQuest(startTiles = startTiles).start()
        val completedQuest = mainQuest(startTiles = startTiles, tasks = listOf(defeat(unitId = "goat-wizard"))).start().defeated("goat-wizard")
        // When / Then
        activeQuest.canBeStartedOn(startTiles) shouldBe false
        completedQuest.canBeStartedOn(startTiles) shouldBe false
    }

    @Test
    fun `Quest should be active on its first task and announce it when it starts`() {
        // Given
        val quest = mainQuest(id = "defeat-the-goat-wizard")
        // When
        val startedQuest = quest.start()
        // Then
        startedQuest.toDto().progress shouldBe ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false)
        startedQuest.pullEvents().first.shouldContainExactly(QuestStarted(questId = "defeat-the-goat-wizard"))
    }

    @Test
    fun `Quest should move on to the next task when the current one is done`() {
        // Given
        val quest = started(tasks = listOf(visit(1 to 1), defeat(unitId = "rat")))
        // When
        val updatedQuest = quest.visited(listOf(PositionDto(row = 1, column = 1)))
        // Then
        updatedQuest.toDto().progress shouldBe ProgressDto(questId = "a-quest", status = StatusDto.ACTIVE, currentTaskIndex = 1, currentTaskDefeats = 0, isPinned = false)
        updatedQuest.pullEvents().first.shouldContainExactly(TaskCompleted(questId = "a-quest", taskIndex = 0))
    }

    @Test
    fun `Quest should not count an event when its task is not the current one`() {
        // Given
        val quest = started(tasks = listOf(visit(1 to 1), defeat(unitId = "rat")))
        // When
        val updatedQuest = quest.defeated("rat")
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should not count an event when the current task asks for something else`() {
        // Given
        val quest = started(tasks = listOf(defeat(unitId = "rat")))
        // When
        val updatedQuest = quest.defeated("bat").visited(listOf(PositionDto(row = 1, column = 1)))
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should not count an event when it has not started`() {
        // Given
        val quest = mainQuest(tasks = listOf(defeat(unitId = "rat")))
        // When
        val updatedQuest = quest.defeated("rat")
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should complete only once the defeats counted reach the required defeats`() {
        // Given
        val quest = started(tasks = listOf(defeat(unitId = "rat", requiredDefeats = 2)))
        // When
        val onceDefeated = quest.defeated("rat").pullEvents()
        val twiceDefeated = onceDefeated.second.defeated("rat").pullEvents()
        // Then
        onceDefeated.second.toDto().progress shouldBe ProgressDto(questId = "a-quest", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskDefeats = 1, isPinned = false)
        onceDefeated.first.shouldContainExactly(DefeatCounted(questId = "a-quest", taskIndex = 0, defeats = 1, requiredDefeats = 2))
        twiceDefeated.second
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
        twiceDefeated.first.shouldContainExactly(
            DefeatCounted(questId = "a-quest", taskIndex = 0, defeats = 2, requiredDefeats = 2),
            TaskCompleted(questId = "a-quest", taskIndex = 0),
            QuestCompleted(questId = "a-quest"),
        )
    }

    @Test
    fun `Quest should complete when the last task is done`() {
        // Given
        val quest = started(tasks = listOf(visit(1 to 1)))
        // When
        val updatedQuest = quest.visited(listOf(PositionDto(row = 0, column = 0), PositionDto(row = 1, column = 1)))
        // Then
        updatedQuest.toDto().progress.status shouldBe StatusDto.COMPLETED
        updatedQuest.pullEvents().first.shouldContainExactly(TaskCompleted(questId = "a-quest", taskIndex = 0), QuestCompleted(questId = "a-quest"))
    }

    @Test
    fun `Quest should complete the task when the talk is with its npc`() {
        // Given
        val quest = started(tasks = listOf(talk(npcId = "elder")))
        // When
        val updatedQuest = quest.talkedTo("elder")
        // Then
        updatedQuest.toDto().progress.status shouldBe StatusDto.COMPLETED
    }

    @Test
    fun `Quest should not complete the task when the talk is with another npc`() {
        // Given
        val quest = started(tasks = listOf(talk(npcId = "elder")))
        // When
        val updatedQuest = quest.talkedTo("merchant")
        // Then
        updatedQuest shouldBe quest
    }

    @Test
    fun `Quest should not count an event when it is completed`() {
        // Given
        val completedQuest = started(tasks = listOf(defeat(unitId = "rat"))).defeated("rat").pullEvents().second
        // When
        val updatedQuest = completedQuest.defeated("rat")
        // Then
        updatedQuest shouldBe completedQuest
    }

    @Test
    fun `Quest should be startable by its quest giver when it is not started`() {
        // Given
        val quest = sideQuest(questGiverNpcId = "farmer")
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe true
    }

    @Test
    fun `Quest should not be startable by another npc`() {
        // Given
        val quest = sideQuest(questGiverNpcId = "farmer")
        // When / Then
        quest.canBeStartedBy("blacksmith") shouldBe false
    }

    @Test
    fun `Quest should not be startable by its quest giver when it is active`() {
        // Given
        val quest = sideQuest(questGiverNpcId = "farmer", repeatable = true).start()
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe false
    }

    @Test
    fun `Quest should be startable by its quest giver when it is completed and repeatable`() {
        // Given
        val quest = sideQuest(questGiverNpcId = "farmer", repeatable = true, tasks = listOf(defeat(unitId = "rat"))).start().defeated("rat")
        // When / Then
        quest.canBeStartedBy("farmer") shouldBe true
    }

    @Test
    fun `Quest should not be startable by its quest giver when it is completed and not repeatable`() {
        // Given
        val quest = sideQuest(questGiverNpcId = "farmer", tasks = listOf(defeat(unitId = "rat"))).start().defeated("rat")
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
                tasks = listOf(visit(1 to 1), defeat(unitId = "rat", requiredDefeats = 5)),
            ).start().visited(listOf(PositionDto(row = 1, column = 1))).defeated("rat").pullEvents().second
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
        val (events, pinned) = started(listOf(defeat())).pin().pullEvents()
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
        val pinned = started(listOf(defeat())).pin().pullEvents().second
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
        val pinned = started(listOf(defeat(unitId = "rat", requiredDefeats = 1))).pin().pullEvents().second
        // When
        val completed = pinned.defeated("rat")
        // Then
        completed.toDto().progress.status shouldBe StatusDto.COMPLETED
        completed.toDto().progress.isPinned shouldBe false
    }

    @Test
    fun `Defeated should keep the pin when a task is left`() {
        // Given
        val pinned = started(listOf(defeat(unitId = "rat", requiredDefeats = 1), talk("farmer"))).pin().pullEvents().second
        // When
        val progressed = pinned.defeated("rat")
        // Then
        progressed.toDto().progress.isPinned shouldBe true
    }

    @Test
    fun `MarkerFor should be a talk task when the current task is to talk to the npc`() {
        // Given
        val quest = started(listOf(talk("elder")))
        // When
        val result = quest.markerFor("elder")
        // Then
        result?.sign shouldBe QuestMarkerDto.SignDto.TALK_TASK
    }

    @Test
    fun `MarkerFor should be nothing when the talk is not the current task`() {
        // Given
        val quest = started(listOf(defeat(), talk("elder")))
        // When
        val result = quest.markerFor("elder")
        // Then
        result shouldBe null
    }

    @Test
    fun `MarkerFor should be a quest to start when the npc can start it`() {
        // When
        val result = sideQuest(questGiverNpcId = "farmer").markerFor("farmer")
        // Then
        result?.sign shouldBe QuestMarkerDto.SignDto.QUEST_TO_START
    }

    @Test
    fun `MarkerFor should be nothing when the quest is under way or another npc is asked`() {
        // Given
        val quest = sideQuest(questGiverNpcId = "farmer")
        // When
        val underWay =
            quest
                .start()
                .pullEvents()
                .second
                .markerFor("farmer")
        val another = quest.markerFor("elder")
        // Then
        underWay shouldBe null
        another shouldBe null
    }

    @Test
    fun `MarkerFor should be a quest to start when a repeatable quest is completed`() {
        // Given
        val completed =
            sideQuest(questGiverNpcId = "farmer", repeatable = true, tasks = listOf(defeat(unitId = "rat", requiredDefeats = 1)))
                .start()
                .defeated("rat")
                .pullEvents()
                .second
        // When
        val result = completed.markerFor("farmer")
        // Then
        result?.sign shouldBe QuestMarkerDto.SignDto.QUEST_TO_START
    }

    @Test
    fun `LineFor should be the start line when the npc is the quest giver of a quest not started`() {
        // Given
        val quest = sideQuest(id = "bats", questGiverNpcId = "farmer")
        // When
        val result = quest.lineFor("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "bats", kind = KindDto.START, taskIndex = null)
    }

    @Test
    fun `LineFor should be the reminder line when the quest is active and its current task is no talk with its quest giver`() {
        // Given
        val quest = sideQuest(id = "bats", questGiverNpcId = "farmer", tasks = listOf(defeat(unitId = "bat"), talk("farmer"))).start()
        // When
        val result = quest.lineFor("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "bats", kind = KindDto.REMINDER, taskIndex = null)
    }

    @Test
    fun `LineFor should be the talk task line, with the place of its task, when the current task is to talk to the npc`() {
        // Given
        val quest =
            sideQuest(id = "bats", questGiverNpcId = "farmer", tasks = listOf(defeat(unitId = "bat"), talk("farmer")))
                .start()
                .defeated("bat")
        // When
        val result = quest.lineFor("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "bats", kind = KindDto.TALK_TASK, taskIndex = 1)
    }

    @Test
    fun `LineFor should be the talk task line when the npc of the current talk task is no quest giver`() {
        // Given
        val quest = mainQuest(id = "goat-wizard", tasks = listOf(talk("elder"), defeat())).start()
        // When
        val result = quest.lineFor("elder")
        // Then
        result shouldBe QuestLineDto(questId = "goat-wizard", kind = KindDto.TALK_TASK, taskIndex = 0)
    }

    @Test
    fun `LineFor should be the completed line when the quest is completed and not repeatable`() {
        // Given
        val quest =
            sideQuest(id = "bats", questGiverNpcId = "farmer", tasks = listOf(defeat(unitId = "bat")))
                .start()
                .defeated("bat")
        // When
        val result = quest.lineFor("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "bats", kind = KindDto.COMPLETED, taskIndex = null)
    }

    @Test
    fun `LineFor should be the start again line when a repeatable quest is completed`() {
        // Given
        val quest =
            sideQuest(id = "rats", questGiverNpcId = "elf", repeatable = true, tasks = listOf(defeat(unitId = "rat")))
                .start()
                .defeated("rat")
        // When
        val result = quest.lineFor("elf")
        // Then
        result shouldBe QuestLineDto(questId = "rats", kind = KindDto.START_AGAIN, taskIndex = null)
    }

    @Test
    fun `LineFor should be the start line again when a repeatable quest completed before is abandoned`() {
        // Given
        val quest =
            sideQuest(id = "rats", questGiverNpcId = "elf", repeatable = true, tasks = listOf(defeat(unitId = "rat")))
                .start()
                .defeated("rat")
                .start()
                .abandon()
        // When
        val result = quest.lineFor("elf")
        // Then
        result shouldBe QuestLineDto(questId = "rats", kind = KindDto.START, taskIndex = null)
    }

    @Test
    fun `LineFor should be nothing when the npc is neither the quest giver nor the npc of the current talk task`() {
        // Given
        val sideQuest = sideQuest(questGiverNpcId = "farmer", tasks = listOf(defeat(), talk("elder"))).start()
        val mainQuest = mainQuest(tasks = listOf(defeat(), talk("elder"))).start()
        // When
        val results = listOf(sideQuest.lineFor("elder"), sideQuest.lineFor("viking"), mainQuest.lineFor("elder"))
        // Then
        results shouldBe listOf(null, null, null)
    }
}
