package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reachTiles
import com.mkz.rpg.quest.domain.QuestMother.reward
import com.mkz.rpg.quest.domain.QuestMother.talkToNpc
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Kind
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.RewardItem
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuestMapperTest {
    private val goatWizardQuestId = "defeat-the-goat-wizard"
    private val threeTasks = listOf(talkToNpc("elder"), defeatUnits(unitId = "rat", count = 5), reachTiles(1 to 1))

    private fun questInfo(
        quest: Quest,
        status: StatusDto = StatusDto.ACTIVE,
        currentTaskIndex: Int = 0,
        currentTaskProgress: Int = 0,
    ): QuestInfo {
        val id = quest.toDto().id
        return QuestMapper.questInfo(quest.restoreProgress(ProgressDto(id, status, currentTaskIndex, currentTaskProgress)).toDto(), TextsMother.texts())
    }

    @Test
    fun `questInfo should translate the name, the description and each task when given a quest`() {
        // Given
        val quest = mainQuest(id = goatWizardQuestId, tasks = listOf(defeatUnits()))
        // When
        val spanish = QuestMapper.questInfo(quest.toDto(), TextsMother.texts("es"))
        val english = questInfo(quest)
        // Then
        english.name shouldBe "The Goat Wizard's Lair"
        english.description shouldBe "A goat swallowed a wizard's magic and now rules the lair down south."
        english.tasks.single().text shouldBe "Defeat the Goat Wizard"
        spanish.name shouldBe "La guarida del Mago Cabra"
        spanish.tasks.single().text shouldBe "Derrota al Mago Cabra"
    }

    @Test
    fun `questInfo should map the tasks before the current one as done, and none after it, when a task is under way`() {
        // Given
        val quest = mainQuest(tasks = threeTasks)
        // When
        val info = questInfo(quest, currentTaskIndex = 1, currentTaskProgress = 3)
        // Then
        info.isCompleted.shouldBeFalse()
        info.tasks.map { task -> task.isDone }.shouldContainExactly(true, false)
        info.doneTasks.map { task -> task.kind }.shouldContainExactly(Kind.TALK)
        info.currentTask.kind shouldBe Kind.DEFEAT
        info.currentTask.progress shouldBe 3
    }

    @Test
    fun `questInfo should map every task as done when the quest is completed`() {
        // Given
        val quest = mainQuest(tasks = threeTasks)
        // When
        val info = questInfo(quest, status = StatusDto.COMPLETED, currentTaskIndex = 2)
        // Then
        info.isCompleted.shouldBeTrue()
        info.tasks.map { task -> task.isDone }.shouldContainExactly(true, true, true)
        info.tasks.map { task -> task.progress }.shouldContainExactly(1, 5, 1)
        info.currentTask.kind shouldBe Kind.REACH
    }

    @Test
    fun `questInfo should count the defeats of a defeat task and 1 for a talk or a reach`() {
        // Given
        val quest = mainQuest(tasks = threeTasks)
        // When
        val info = questInfo(quest, status = StatusDto.COMPLETED, currentTaskIndex = 2)
        // Then
        info.tasks.map { task -> task.count }.shouldContainExactly(1, 5, 1)
    }

    @Test
    fun `questInfo should point to the portrait of the unit, of the npc, or to none, by the kind of task`() {
        // Given
        val quest = mainQuest(tasks = threeTasks)
        // When
        val info = questInfo(quest, status = StatusDto.COMPLETED, currentTaskIndex = 2)
        // Then
        info.tasks.map { task -> task.portraitPath }.shouldContainExactly("npc/elder_portrait.png", "unit/rat_portrait.png", null)
    }

    @Test
    fun `questInfo should name the gold and each item when the reward has them`() {
        // Given
        val quest = mainQuest(reward = reward(gold = 40, lootableItemIds = listOf("health-potion", "hammer")))
        // When
        val info = questInfo(quest)
        // Then
        info.gold shouldBe 40
        info.items.shouldContainExactly(RewardItem("health-potion", TextsMother.texts().lootableItemName("health-potion")), RewardItem("hammer", TextsMother.texts().lootableItemName("hammer")))
    }
}
