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

class WalkBattleUnitInBattleTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val walkBattleUnitInBattle =
        WalkBattleUnitInBattle(
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
            searchPosition = queries.searchPosition,
            searchShortestPath = queries.searchShortestPath,
        )

    @Test
    fun `should walk battle unit along the shortest path when a reachable position is provided`() {
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
        walkBattleUnitInBattle(battleUnitId = "knight-1", walkToRow = 2, walkToColumn = 1)
        // Then
        val storedBattleUnit = queries.battleUnitRepository.searchById("knight-1")?.toDto()
        storedBattleUnit!!.remainingActions.remainingSteps shouldBe 2
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.BattleUnitMoved("knight-1", 0, 0, 2, 1, listOf(PositionDto(1, 0), PositionDto(2, 0), PositionDto(2, 1))),
            BattlefieldEvent.RequestMoveOccupant("knight-1", 2, 1),
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
        walkBattleUnitInBattle(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 2)
        // Then
        val storedBattleUnit = queries.battleUnitRepository.searchById("knight-1")?.toDto()
        storedBattleUnit!!.remainingActions.remainingSteps shouldBe 1
    }

    @Test
    fun `should not walk battle unit when there is no path to the destination`() {
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
        walkBattleUnitInBattle(battleUnitId = "knight-1", walkToRow = 2, walkToColumn = 1)
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
        queries.battleUnitRepository
            .searchById("knight-1")
            ?.toDto()
            ?.remainingActions
            ?.remainingSteps shouldBe 5
    }

    @Test
    fun `should not walk battle unit when it does not exist`() {
        // Given
        // When
        walkBattleUnitInBattle(battleUnitId = "unknown-battle-unit", walkToRow = 1, walkToColumn = 0)
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not walk battle unit when the position is not found`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        walkBattleUnitInBattle(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 1)
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
