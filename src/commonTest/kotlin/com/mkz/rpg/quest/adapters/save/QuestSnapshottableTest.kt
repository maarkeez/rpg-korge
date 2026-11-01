package com.mkz.rpg.quest.adapters.save

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.commands.RestoreQuests
import com.mkz.rpg.quest.usecases.queries.SearchQuestsSnapshot
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuestSnapshottableTest {
    private fun snapshottable(repository: QuestRepository) = QuestSnapshottable(SearchQuestsSnapshot(repository), RestoreQuests(repository))

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val main = mainQuest(id = "defeat-the-goat-wizard")
        val side = sideQuest(id = "help-the-farmer")
        val played =
            InMemoryQuestRepository().apply {
                save(main.start())
                save(side)
            }
        val restored =
            InMemoryQuestRepository().apply {
                save(main)
                save(side)
            }
        // When
        snapshottable(restored).restore(snapshottable(played).snapshot())
        // Then
        restored.searchAll().map { quest -> quest.toDto() } shouldBe played.searchAll().map { quest -> quest.toDto() }
    }

    @Test
    fun `Key should be quest`() {
        // When
        val result = snapshottable(InMemoryQuestRepository()).key
        // Then
        result shouldBe "quest"
    }
}
