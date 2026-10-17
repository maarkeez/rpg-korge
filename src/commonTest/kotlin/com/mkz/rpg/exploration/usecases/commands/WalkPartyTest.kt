package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.services.PartyWalking
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class WalkPartyTest {
    private val queries = InMemoryQueries()
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val walkParty =
        WalkParty(
            explorationRepository = explorationRepository,
            searchPosition = queries.searchPosition,
            partyWalking = PartyWalking(explorationRepository, queries.searchPosition, queries.searchShortestPath, eventBus),
        )

    private fun givenParty(map: String) {
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'R' to "rat-1")))
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1", "knight-2")))
    }

    @Test
    fun `should request the leader walk one tile towards the direction when the party explores`() {
        // Given
        givenParty(
            """
            . .
            K .
            L .
            """,
        )
        // When
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0))
        explorationRepository
            .search()!!
            .toDto()
            .isWalking
            .shouldBeTrue()
    }

    @Test
    fun `should walk the leader east when the east arrow is held`() {
        // Given
        givenParty(
            """
            K .
            L .
            """,
        )
        // When
        walkParty(DirectionDto.EAST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 1))
    }

    @Test
    fun `should not walk when the leader can not occupy the next tile`() {
        // Given
        givenParty(
            """
            # R
            K .
            L .
            """,
        )
        // When
        walkParty(DirectionDto.NORTH)
        walkParty(DirectionDto.WEST)
        // Then
        eventBus.shouldNotHavePublishedEvents()
        explorationRepository
            .search()!!
            .toDto()
            .isWalking
            .shouldBeFalse()
    }

    @Test
    fun `should not walk when the leader would leave the battlefield`() {
        // Given
        givenParty(
            """
            K .
            L .
            """,
        )
        // When
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not walk again when the party is still walking`() {
        // Given
        givenParty(
            """
            . .
            K .
            L .
            """,
        )
        walkParty(DirectionDto.NORTH)
        // When
        walkParty(DirectionDto.EAST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0))
    }

    @Test
    fun `should not walk when the party is battling`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". .\nK .", occupants = mapOf('K' to "knight-1")))
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1")).pause().pullEvents().second)
        // When
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
