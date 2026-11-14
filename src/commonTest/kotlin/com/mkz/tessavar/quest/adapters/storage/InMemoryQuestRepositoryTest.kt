package com.mkz.tessavar.quest.adapters.storage

import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryQuestRepositoryTest {
    private val questRepository = InMemoryQuestRepository()

    @Test
    fun `Save should store the quest`() {
        // Given
        val quest = mainQuest(id = "defeat-the-goat-wizard")
        // When
        questRepository.save(quest)
        // Then
        questRepository.searchById("defeat-the-goat-wizard") shouldBe quest
    }

    @Test
    fun `Save should replace the quest when it is saved again`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard"))
        val startedQuest = mainQuest(id = "defeat-the-goat-wizard").start()
        // When
        questRepository.save(startedQuest)
        // Then
        questRepository.searchAll().shouldContainExactly(startedQuest)
    }

    @Test
    fun `SearchById should return nothing when the quest does not exist`() {
        // When / Then
        questRepository.searchById("missing").shouldBeNull()
    }

    @Test
    fun `SearchAll should return every quest in the order they were created`() {
        // Given
        val main = mainQuest()
        val side = sideQuest()
        questRepository.save(main)
        questRepository.save(side)
        // When / Then
        questRepository.searchAll().shouldContainExactly(main, side)
    }
}
