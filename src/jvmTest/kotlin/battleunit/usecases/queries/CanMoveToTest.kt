package battleunit.usecases.queries

import battlefield.domain.Battlefield.Dto.PositionDto
import battlefield.usecases.queries.SearchPosition
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother.battleUnit
import battleunit.usecases.services.DistanceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import unit.domain.UnitMother.unit

class CanMoveToTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchPosition: SearchPosition = mock()
    private val distanceService = DistanceService()
    private val canMoveTo = CanMoveTo(battleUnitRepository, searchPosition, distanceService)

    @Test
    fun `should return true when the distance is within the remaining steps`() {
        // Given
        val battleUnit =
            battleUnit(
                unit =
                    unit(movementRange = 5)
                        .toDto(),
            )
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the distance exceeds the remaining steps`() {
        // Given
        val battleUnit = battleUnit(unit = unit(movementRange = 2).toDto())
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(0, 0))
        // When
        val result = canMoveTo(battleUnitId = battleUnit.toDto().id, moveToRow = 3, moveToColumn = 0)
        // Then
        assertThat(result).isFalse()
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
