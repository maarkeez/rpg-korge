package com.mkz.tessavar.quest.usecases.queries

import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchQuestsTest {
    private val questRepository = InMemoryQuestRepository()
    private val searchQuests = SearchQuests(questRepository)

    @Test
    fun `SearchQuests should return every quest in the order they were created`() {
        // Given
        val main = mainQuest()
        val side = sideQuest()
        questRepository.save(main)
        questRepository.save(side)
        // When
        val result = searchQuests()
        // Then
        result.shouldContainExactly(main.toDto(), side.toDto())
    }
}
