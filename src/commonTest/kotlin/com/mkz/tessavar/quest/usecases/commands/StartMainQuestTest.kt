package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestEvent.QuestStarted
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class StartMainQuestTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val startMainQuest = StartMainQuest(questRepository, queries.searchParty, PublishQuestProgress(questRepository, eventBus))

    private val entrance = listOf(PositionDto(row = 14, column = 9), PositionDto(row = 14, column = 10))

    private fun status(questId: String) =
        questRepository
            .searchById(questId)!!
            .toDto()
            .progress.status

    @BeforeTest
    fun setUp() {
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard", startTiles = entrance))
        questRepository.save(sideQuest(id = "help-the-farmer"))
    }

    @Test
    fun `StartMainQuest should start the main quest when a party member walks onto a start tile`() {
        // When
        startMainQuest(battleUnitId = "knight-2", path = listOf(PositionDto(row = 13, column = 10), PositionDto(row = 14, column = 10)))
        // Then
        status("defeat-the-goat-wizard") shouldBe StatusDto.ACTIVE
        status("help-the-farmer") shouldBe StatusDto.NOT_STARTED
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "defeat-the-goat-wizard"))
    }

    @Test
    fun `StartMainQuest should start the main quest when a party member teleports onto a start tile`() {
        // When
        startMainQuest(battleUnitId = "knight-1", path = listOf(PositionDto(row = 14, column = 9)))
        // Then
        status("defeat-the-goat-wizard") shouldBe StatusDto.ACTIVE
    }

    @Test
    fun `StartMainQuest should not start the main quest when no tile of the path is a start tile`() {
        // When
        startMainQuest(battleUnitId = "knight-1", path = listOf(PositionDto(row = 15, column = 10)))
        // Then
        status("defeat-the-goat-wizard") shouldBe StatusDto.NOT_STARTED
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `StartMainQuest should not start the main quest when the battle unit is no party member`() {
        // When
        startMainQuest(battleUnitId = "bat-1", path = entrance)
        // Then
        status("defeat-the-goat-wizard") shouldBe StatusDto.NOT_STARTED
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `StartMainQuest should not start the main quest again when it is active`() {
        // Given
        startMainQuest(battleUnitId = "knight-1", path = entrance)
        // When
        startMainQuest(battleUnitId = "knight-1", path = entrance)
        // Then
        eventBus.shouldHavePublishedEvents(QuestStarted(questId = "defeat-the-goat-wizard"))
    }
}
