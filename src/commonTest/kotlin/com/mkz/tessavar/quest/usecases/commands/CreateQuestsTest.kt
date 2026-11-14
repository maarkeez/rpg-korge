package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.tessavar.quest.domain.QuestError.QuestTileNotVacant
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.domain.QuestMother.visit
import com.mkz.tessavar.quest.usecases.services.FakeQuestLoader
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateQuestsTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()

    private fun createQuests(vararg quests: Quest.Dto) = CreateQuests(FakeQuestLoader(quests.toList()), questRepository, queries.isTileVacant).invoke()

    @BeforeTest
    fun setUp() {
        queries.battlefieldRepository.create(battlefield(". ."))
    }

    @Test
    fun `CreateQuests should create every loaded quest`() {
        // Given
        val main = mainQuest(id = "defeat-the-goat-wizard").toDto()
        val side = sideQuest(id = "help-the-farmer", tasks = listOf(visit(0 to 1))).toDto()
        // When
        createQuests(main, side)
        // Then
        questRepository.searchById("defeat-the-goat-wizard")?.toDto() shouldBe main
        questRepository.searchById("help-the-farmer")?.toDto() shouldBe side
    }

    @Test
    fun `CreateQuests should not create a quest again when it already exists`() {
        // Given
        val startedQuest = mainQuest(id = "defeat-the-goat-wizard").start().pullEvents().second
        questRepository.save(startedQuest)
        // When
        createQuests(mainQuest(id = "defeat-the-goat-wizard").toDto())
        // Then
        questRepository.searchById("defeat-the-goat-wizard") shouldBe startedQuest
    }

    @Test
    fun `CreateQuests should fail when there is no main quest`() {
        shouldThrow<MainQuestNotUnique> { createQuests(sideQuest().toDto()) }
    }

    @Test
    fun `CreateQuests should fail when there are two main quests`() {
        shouldThrow<MainQuestNotUnique> { createQuests(mainQuest().toDto(), mainQuest().toDto()) }
    }

    @Test
    fun `CreateQuests should fail when a tile of a visit task is not vacant`() {
        shouldThrow<QuestTileNotVacant> { createQuests(mainQuest(tasks = listOf(visit(0 to 0, 9 to 9))).toDto()) }
    }

    @Test
    fun `CreateQuests should fail when a start tile is not vacant`() {
        shouldThrow<QuestTileNotVacant> { createQuests(mainQuest(startTiles = listOf(PositionDto(row = 0, column = 0), PositionDto(row = 9, column = 9))).toDto()) }
    }
}
