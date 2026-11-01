package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class RestoreQuestsTest {
    private val questRepository = InMemoryQuestRepository()
    private val restoreQuests = RestoreQuests(questRepository)

    @Test
    fun `RestoreQuests should put back the progress of each quest, publishing no events`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard"))
        questRepository.save(sideQuest(id = "clear-the-rats", tasks = listOf(defeatUnits(unitId = "bat"), defeatUnits(unitId = "rat", count = 5))))
        val mainProgress = ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.COMPLETED, currentTaskIndex = 0, currentTaskProgress = 1, isPinned = false)
        val sideProgress = ProgressDto(questId = "clear-the-rats", status = StatusDto.ACTIVE, currentTaskIndex = 1, currentTaskProgress = 3, isPinned = false)
        // When
        restoreQuests(listOf(mainProgress, sideProgress))
        // Then
        val restoredMain = questRepository.searchById("defeat-the-goat-wizard")!!
        restoredMain.toDto().progress shouldBe mainProgress
        restoredMain.pullEvents().first.shouldBeEmpty()
        questRepository.searchById("clear-the-rats")!!.toDto().progress shouldBe sideProgress
    }

    @Test
    fun `RestoreQuests should fail when the quest no longer exists`() {
        // When
        val result = runCatching { restoreQuests(listOf(ProgressDto.notStarted("old-quest"))) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "old-quest"
    }

    @Test
    fun `RestoreQuests should fail when the task no longer exists`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard"))
        // When
        val result = runCatching { restoreQuests(listOf(ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.ACTIVE, currentTaskIndex = 1, currentTaskProgress = 0, isPinned = false))) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "defeat-the-goat-wizard"
    }
}
