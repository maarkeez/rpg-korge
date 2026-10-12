package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CanMoveToTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchPosition: SearchPosition = mock()
    private val searchShortestPath: SearchShortestPath = mock()
    private val canMoveTo = CanMoveTo(battleUnitRepository, searchPosition, searchShortestPath)

    private val threeTilesPath = listOf(PositionDto(1, 0), PositionDto(2, 0), PositionDto(3, 0))

    @Test
    fun `should return true when the shortest path is within the remaining steps`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 3).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)).thenReturn(threeTilesPath)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the shortest path exceeds the remaining steps`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 2).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)).thenReturn(threeTilesPath)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when there is no path to the destination`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 3).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)).thenReturn(null)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        whenever(searchPosition("unknown-battle-unit")).thenReturn(PositionDto(0, 0))
        // When
        val result = canMoveTo(battleUnitId = "unknown-battle-unit", moveToRow = 1, moveToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the position is not found`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(null)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 1, moveToColumn = 0)
        // Then
        result.shouldBeFalse()
    }
}
