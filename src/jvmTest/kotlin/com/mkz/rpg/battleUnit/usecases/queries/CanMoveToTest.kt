package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CanMoveToTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchPosition: SearchPosition = mock()
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val canMoveTo = CanMoveTo(battleUnitRepository, searchPosition, canBattlefieldTileBeOccupied)

    @Test
    fun `should return true when the shortest path distance is within the remaining steps`() {
        // Given
        val battleUnit =
            battleUnit(
                unit =
                    unit(movementRange = 5)
                        .toDto(),
            )
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(canBattlefieldTileBeOccupied(1, 0)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(2, 0)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(3, 0)).thenReturn(true)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the shortest path distance exceeds the remaining steps`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 2).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(canBattlefieldTileBeOccupied(1, 0)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(2, 0)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(3, 0)).thenReturn(true)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the destination can only be reached jumping over non occupiable tiles`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 3).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(canBattlefieldTileBeOccupied(3, 0)).thenReturn(true)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when a path exists longer than the straight line distance`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 4).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        whenever(canBattlefieldTileBeOccupied(1, 0)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(1, 1)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(1, 2)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(0, 2)).thenReturn(true)
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 0, moveToColumn = 2)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        whenever(searchPosition("unknown-battle-unit")).thenReturn(PositionDto(0, 0))
        // When
        val result = canMoveTo(battleUnitId = "unknown-battle-unit", moveToRow = 1, moveToColumn = 0)
        // Then
        assertThat(result).isFalse()
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
        assertThat(result).isFalse()
    }
}
