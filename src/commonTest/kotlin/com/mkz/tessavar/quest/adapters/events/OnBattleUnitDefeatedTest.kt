package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestMother.defeat
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.usecases.commands.ProgressDefeatTasks
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(ProgressDefeatTasks(questRepository, queries.searchParty, PublishQuestProgress(questRepository, eventBus)), eventBus)

    @Test
    fun `should progress the defeat tasks when a battle unit is defeated`() {
        // Given
        queries.partyRepository.save(party(playerId = "human"))
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard", tasks = listOf(defeat(unitId = "goat-wizard"))).start().pullEvents().second)
        eventBus.publish(BattleUnitDefeated(playerId = "cpu", battleUnitId = "goat-wizard-1", unitId = "goat-wizard", defeatedAtRow = 36, defeatedAtColumn = 11))
        // When
        eventBus.dispatch()
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.status shouldBe StatusDto.COMPLETED
    }
}
