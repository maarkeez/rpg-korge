package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.Exploration.Dto.StepDto
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.services.PartyWalking
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResolvePartyStepTest {
    private val queries = InMemoryQueries()
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val resolvePartyStep =
        ResolvePartyStep(
            explorationRepository = explorationRepository,
            partyWalking = PartyWalking(explorationRepository, queries.searchPosition, queries.searchShortestPath, eventBus),
        )

    // The leader walked north from (1,0) to (0,0), and the follower at (2,0) walks to the tile the leader left.
    private fun givenLeaderWalkedNorth(map: String) {
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'R' to "rat-1")))
        explorationRepository.save(
            exploration(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2"))
                .walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 0), StepDto(battleUnitId = "knight-2", row = 1, column = 0)))
                .awaitNextStep(),
        )
    }

    @Test
    fun `should request the follower walk to the tile the leader left when the leader has walked`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            . .
            L .
            """,
        )
        // When
        resolvePartyStep(battleUnitId = "knight-1", row = 0, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-2", walkToRow = 1, walkToColumn = 0))
    }

    @Test
    fun `should finish walking when the follower has walked`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            . .
            L .
            """,
        )
        resolvePartyStep(battleUnitId = "knight-1", row = 0, column = 0)
        queries.battlefieldRepository.create(battlefield("K .\nL .\n. .", occupants = mapOf('K' to "knight-1", 'L' to "knight-2")))
        // When
        resolvePartyStep(battleUnitId = "knight-2", row = 1, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-2", walkToRow = 1, walkToColumn = 0),
            ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
        explorationRepository.search()!!.canWalk().shouldBeTrue()
    }

    @Test
    fun `should skip the follower and finish walking when the follower can not reach the tile the leader left`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            R .
            L .
            """,
        )
        // When
        resolvePartyStep(battleUnitId = "knight-1", row = 0, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")))
        explorationRepository.search()!!.canWalk().shouldBeTrue()
    }

    @Test
    fun `should not continue when another tile is occupied`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            . .
            L .
            """,
        )
        // When
        resolvePartyStep(battleUnitId = "rat-1", row = 0, column = 1)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
