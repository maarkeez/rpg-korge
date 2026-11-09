package com.mkz.rpg.quest.adapters.save

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.commands.LoadQuestsSnapshot
import com.mkz.rpg.quest.usecases.queries.SearchQuestsSnapshot
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuestSnapshottableTest {
    private fun snapshottable(repository: QuestRepository) = QuestSnapshottable(SearchQuestsSnapshot(repository), LoadQuestsSnapshot(repository))

    @Test
    fun `Load should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val main = mainQuest(id = "defeat-the-goat-wizard")
        val side = sideQuest(id = "help-the-farmer")
        val played =
            InMemoryQuestRepository().apply {
                save(main.start())
                save(side)
            }
        val loaded =
            InMemoryQuestRepository().apply {
                save(main)
                save(side)
            }
        // When
        snapshottable(loaded).load(snapshottable(played).snapshot())
        // Then
        loaded.searchAll().map { quest -> quest.toDto() } shouldBe played.searchAll().map { quest -> quest.toDto() }
    }

    @Test
    fun `Load should pin the quest when it was saved pinned`() {
        // Given
        val side = sideQuest(id = "help-the-farmer")
        val played = InMemoryQuestRepository().apply { save(side.start().pin()) }
        val loaded = InMemoryQuestRepository().apply { save(side) }
        // When
        snapshottable(loaded).load(snapshottable(played).snapshot())
        // Then
        loaded
            .searchById("help-the-farmer")!!
            .toDto()
            .progress.isPinned shouldBe true
    }

    @Test
    fun `Key should be quest`() {
        // When
        val result = snapshottable(InMemoryQuestRepository()).key
        // Then
        result shouldBe "quest"
    }
}
