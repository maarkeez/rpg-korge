package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.visit
import com.mkz.tessavar.quest.usecases.commands.ProgressVisitTasks
import com.mkz.tessavar.quest.usecases.commands.StartMainQuest
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val publishQuestProgress = PublishQuestProgress(questRepository, eventBus)
    private val onBattleUnitMoved =
        OnBattleUnitMoved(
            StartMainQuest(questRepository, queries.searchParty, publishQuestProgress),
            ProgressVisitTasks(questRepository, queries.searchParty, publishQuestProgress),
            eventBus,
        )

    @Test
    fun `should progress the visit tasks with the tiles walked when a battle unit moves`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        questRepository.save(mainQuest(id = "cross-the-bridge", tasks = listOf(visit(17 to 11))).start().pullEvents().second)
        eventBus.publish(
            BattleUnitMoved(
                battleUnitId = "knight-1",
                fromRow = 16,
                fromColumn = 11,
                toRow = 18,
                toColumn = 11,
                path = listOf(Battlefield.Dto.PositionDto(row = 17, column = 11), Battlefield.Dto.PositionDto(row = 18, column = 11)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        questRepository
            .searchById("cross-the-bridge")!!
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
    }

    @Test
    fun `OnBattleUnitMoved should start the main quest when a party member moves onto a start tile`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard", startTiles = listOf(PositionDto(row = 14, column = 10))))
        eventBus.publish(
            BattleUnitMoved(
                battleUnitId = "knight-1",
                fromRow = 13,
                fromColumn = 10,
                toRow = 15,
                toColumn = 10,
                path = listOf(Battlefield.Dto.PositionDto(row = 14, column = 10), Battlefield.Dto.PositionDto(row = 15, column = 10)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.status shouldBe StatusDto.ACTIVE
    }
}
