package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MoveBattleUnitTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val moveBattleUnit =
        MoveBattleUnit(
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
            searchPosition = queries.searchPosition,
            searchShortestPath = queries.searchShortestPath,
        )

    @Test
    fun `should move battle unit along the shortest path when a reachable position is provided`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 5).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K #
                . #
                . .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        moveBattleUnit(battleUnitId = "knight-1", moveToRow = 2, moveToColumn = 1)
        // Then
        val storedBattleUnit = queries.battleUnitRepository.searchById("knight-1")?.toDto()
        storedBattleUnit!!.remainingTurnActions.remainingSteps shouldBe 2
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.BattleUnitMoved("knight-1", 0, 0, 2, 1, listOf(PositionDto(1, 0), PositionDto(2, 0), PositionDto(2, 1))),
            BattlefieldEvent.RequestMoveOccupant("knight-1", 2, 1, listOf(PositionDto(1, 0), PositionDto(2, 0), PositionDto(2, 1))),
        )
    }

    @Test
    fun `should spend a step per tile walked when the path is longer than the straight line distance`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 5).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K # .
                . . .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        moveBattleUnit(battleUnitId = "knight-1", moveToRow = 0, moveToColumn = 2)
        // Then
        val storedBattleUnit = queries.battleUnitRepository.searchById("knight-1")?.toDto()
        storedBattleUnit!!.remainingTurnActions.remainingSteps shouldBe 1
    }

    @Test
    fun `should not move battle unit when there is no path to the destination`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 5).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K .
                # #
                . .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        moveBattleUnit(battleUnitId = "knight-1", moveToRow = 2, moveToColumn = 1)
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
        queries.battleUnitRepository
            .searchById("knight-1")
            ?.toDto()
            ?.remainingTurnActions
            ?.remainingSteps shouldBe 5
    }

    @Test
    fun `should not move battle unit when it does not exist`() {
        // Given
        // When
        moveBattleUnit(battleUnitId = "unknown-battle-unit", moveToRow = 1, moveToColumn = 0)
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not move battle unit when the position is not found`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        moveBattleUnit(battleUnitId = "knight-1", moveToRow = 0, moveToColumn = 1)
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
