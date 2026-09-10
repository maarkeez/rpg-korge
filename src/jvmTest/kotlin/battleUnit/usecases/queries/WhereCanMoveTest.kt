package battleUnit.usecases.queries

import battleUnit.adapters.storage.InMemoryBattleUnitRepository
import battleUnit.domain.BattleUnitMother.battleUnit
import battlefield.domain.Battlefield.Dto.PositionDto
import battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import unit.domain.UnitMother.unit

class WhereCanMoveTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchTilesThatCanBeOccupied: SearchTilesThatCanBeOccupied = mock()
    private val canMoveTo: CanMoveTo = mock()
    private val whereCanMove = WhereCanMove(battleUnitRepository, searchTilesThatCanBeOccupied, canMoveTo)

    @Test
    fun `should return positions that can be moved to when the battle unit can move`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 2).toDto())
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        val firstPosition = PositionDto(0, 1)
        val secondPosition = PositionDto(0, 2)
        val thirdPosition = PositionDto(1, 0)
        whenever(searchTilesThatCanBeOccupied(battleUnitId, 2))
            .thenReturn(listOf(firstPosition, secondPosition, thirdPosition))
        whenever(canMoveTo(battleUnitId, firstPosition.row, firstPosition.column)).thenReturn(true)
        whenever(canMoveTo(battleUnitId, secondPosition.row, secondPosition.column)).thenReturn(false)
        whenever(canMoveTo(battleUnitId, thirdPosition.row, thirdPosition.column)).thenReturn(true)
        // When
        val result = whereCanMove(battleUnitId)
        // Then
        assertThat(result).containsExactly(firstPosition, thirdPosition)
    }

    @Test
    fun `should return empty list when the battle unit does not exist`() {
        // Given
        // When
        val result = whereCanMove("unknown-battle-unit")
        // Then
        assertThat(result).isEmpty()
    }
}
