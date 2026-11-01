package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reachTiles
import com.mkz.rpg.quest.usecases.commands.ProgressReachTasks
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val onBattleUnitMoved =
        OnBattleUnitMoved(ProgressReachTasks(questRepository, queries.searchParty, PublishQuestProgress(questRepository, eventBus)), eventBus)

    @Test
    fun `should progress the reach tasks with the tiles walked when a battle unit moves`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        questRepository.save(mainQuest(id = "cross-the-bridge", tasks = listOf(reachTiles(17 to 11))).start().pullEvents().second)
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
}
