package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.rpg.quest.domain.QuestError.QuestTileCanNotBeOccupied
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reachTiles
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.usecases.services.FakeQuestLoader
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class InitialiseQuestsTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()

    private fun initialiseQuests(vararg quests: Quest.Dto) = InitialiseQuests(FakeQuestLoader(quests.toList()), questRepository, queries.isTileVacant).invoke()

    @BeforeTest
    fun setUp() {
        queries.battlefieldRepository.create(battlefield(". ."))
    }

    @Test
    fun `InitialiseQuests should create every loaded quest when the quests are initialised`() {
        // Given
        val main = mainQuest(id = "defeat-the-goat-wizard").toDto()
        val side = sideQuest(id = "help-the-farmer", tasks = listOf(reachTiles(0 to 1))).toDto()
        // When
        initialiseQuests(main, side)
        // Then
        questRepository.searchById("defeat-the-goat-wizard")?.toDto() shouldBe main
        questRepository.searchById("help-the-farmer")?.toDto() shouldBe side
    }

    @Test
    fun `InitialiseQuests should not create a quest again when it already exists`() {
        // Given
        val startedQuest = mainQuest(id = "defeat-the-goat-wizard").start().pullEvents().second
        questRepository.save(startedQuest)
        // When
        initialiseQuests(mainQuest(id = "defeat-the-goat-wizard").toDto())
        // Then
        questRepository.searchById("defeat-the-goat-wizard") shouldBe startedQuest
    }

    @Test
    fun `InitialiseQuests should fail when there is no main quest`() {
        shouldThrow<MainQuestNotUnique> { initialiseQuests(sideQuest().toDto()) }
    }

    @Test
    fun `InitialiseQuests should fail when there are two main quests`() {
        shouldThrow<MainQuestNotUnique> { initialiseQuests(mainQuest().toDto(), mainQuest().toDto()) }
    }

    @Test
    fun `InitialiseQuests should fail when a tile can not be occupied`() {
        shouldThrow<QuestTileCanNotBeOccupied> { initialiseQuests(mainQuest(tasks = listOf(reachTiles(0 to 0, 9 to 9))).toDto()) }
    }
}
