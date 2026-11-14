package com.mkz.tessavar.quest.usecases.queries

import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest.Dto.ProgressDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchQuestsSnapshotTest {
    private val questRepository = InMemoryQuestRepository()
    private val searchQuestsSnapshot = SearchQuestsSnapshot(questRepository)

    @Test
    fun `SearchQuestsSnapshot should return how far each quest went`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard").start())
        questRepository.save(sideQuest(id = "help-the-farmer"))
        // When
        val result = searchQuestsSnapshot()
        // Then
        result.shouldContainExactly(
            ProgressDto(questId = "defeat-the-goat-wizard", status = StatusDto.ACTIVE, currentTaskIndex = 0, currentTaskDefeats = 0, isPinned = false),
            ProgressDto.notStarted("help-the-farmer"),
        )
    }
}
