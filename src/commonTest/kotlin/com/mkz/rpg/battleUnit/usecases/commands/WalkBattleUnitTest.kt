package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WalkBattleUnitTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val walkBattleUnit =
        WalkBattleUnit(
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
            searchPosition = queries.searchPosition,
            searchShortestPath = queries.searchShortestPath,
        )

    @Test
    fun `should walk the battle unit along the shortest path without spending steps when the tile can be reached`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 1).toDto()))
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
        walkBattleUnit(battleUnitId = "knight-1", walkToRow = 2, walkToColumn = 1)
        // Then
        queries.battleUnitRepository
            .searchById("knight-1")!!
            .toDto()
            .remainingTurnActions.remainingSteps shouldBe 1
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.BattleUnitMoved("knight-1", 0, 0, 2, 1, listOf(PositionDto(1, 0), PositionDto(2, 0), PositionDto(2, 1))),
            BattlefieldEvent.RequestMoveOccupant("knight-1", 2, 1),
        )
    }

    @Test
    fun `should not walk when the tile can not be reached`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield("K # .", occupants = mapOf('K' to "knight-1")))
        // When
        walkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 2)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not walk when the battle unit is already on the tile`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        // When
        walkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
