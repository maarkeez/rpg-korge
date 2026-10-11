package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MoveBattleUnitTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val searchPosition: SearchPosition = mock()
    private val searchShortestPath: SearchShortestPath = mock()
    private val moveBattleUnit =
        MoveBattleUnit(
            battleUnitRepository = battleUnitRepository,
            eventBus = eventBus,
            searchPosition = searchPosition,
            searchShortestPath = searchShortestPath,
        )

    @Test
    fun `should move battle unit along the shortest path when a reachable position is provided`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 5).toDto())
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        val path = listOf(PositionDto(1, 0), PositionDto(2, 0), PositionDto(2, 1))
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(0, 0))
        whenever(searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 2, toColumn = 1)).thenReturn(path)
        // When
        moveBattleUnit(battleUnitId = battleUnitId, moveToRow = 2, moveToColumn = 1)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto()
        assertThat(storedBattleUnit!!.remainingTurnActions.remainingSteps).isEqualTo(2)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.BattleUnitMoved(battleUnitId, 0, 0, 2, 1, path),
        )
    }

    @Test
    fun `should spend a step per tile walked when the path is longer than the straight line distance`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 5).toDto())
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        val pathAroundAnObstacle = listOf(PositionDto(1, 0), PositionDto(1, 1), PositionDto(1, 2), PositionDto(0, 2))
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(0, 0))
        whenever(searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2)).thenReturn(pathAroundAnObstacle)
        // When
        moveBattleUnit(battleUnitId = battleUnitId, moveToRow = 0, moveToColumn = 2)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto()
        assertThat(storedBattleUnit!!.remainingTurnActions.remainingSteps).isEqualTo(1)
    }

    @Test
    fun `should not move battle unit when there is no path to the destination`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 5).toDto())
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(0, 0))
        whenever(searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 2, toColumn = 1)).thenReturn(null)
        // When
        moveBattleUnit(battleUnitId = battleUnitId, moveToRow = 2, moveToColumn = 1)
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
        assertThat(
            battleUnitRepository
                .searchById(battleUnitId)
                ?.toDto()
                ?.remainingTurnActions
                ?.remainingSteps,
        ).isEqualTo(5)
    }

    @Test
    fun `should not move battle unit when it does not exist`() {
        // Given
        // When
        moveBattleUnit(battleUnitId = "unknown-battle-unit", moveToRow = 1, moveToColumn = 0)
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should not move battle unit when the position is not found`() {
        // Given
        val battleUnit =
            battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(null)
        // When
        moveBattleUnit(battleUnitId = battleUnit.toDto().id, moveToRow = 1, moveToColumn = 0)
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
