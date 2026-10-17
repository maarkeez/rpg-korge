package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.Exploration.Dto.StepDto
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.commands.ResolvePartyStep
import com.mkz.rpg.exploration.usecases.services.PartyWalking
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnBattlefieldTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onBattlefieldTileOccupied =
        OnBattlefieldTileOccupied(
            ResolvePartyStep(explorationRepository, PartyWalking(explorationRepository, queries.searchPosition, queries.searchShortestPath, eventBus)),
            eventBus,
        )

    @Test
    fun `should finish walking when the awaited battle unit occupies its tile`() {
        // Given
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        explorationRepository.save(
            exploration(battleUnitIds = listOf("knight-1"))
                .walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 0)))
                .awaitNextStep(),
        )
        eventBus.publish(BattlefieldEvent.BattlefieldTileOccupied(row = 0, column = 0, battlefieldUnitId = "knight-1"))
        // When
        eventBus.dispatch()
        // Then
        explorationRepository.search()!!.canWalk().shouldBeTrue()
    }
}
