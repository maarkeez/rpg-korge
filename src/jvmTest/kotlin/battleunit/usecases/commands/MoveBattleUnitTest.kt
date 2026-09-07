package battleunit.usecases.commands

import battlefield.domain.Battlefield.Dto.PositionDto
import battlefield.usecases.queries.*
import battleunit.adapters.storage.*
import battleunit.domain.*
import battleunit.usecases.services.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import shared.domain.*
import shared.domain.assertThat
import unit.domain.*

class MoveBattleUnitTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val searchPosition: SearchPosition = mock()
    private val distanceService = DistanceService()
    private val moveBattleUnit = MoveBattleUnit(
        battleUnitRepository = battleUnitRepository,
        eventBus = eventBus,
        searchPosition = searchPosition,
        distanceService = distanceService,
    )

    @Test
    fun `should move battle unit when a reachable position is provided`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 5).toDto())
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(0, 0))
        // When
        moveBattleUnit(battleUnitId = battleUnitId, moveToRow = 2, moveToColumn = 1)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto()
        assertThat(storedBattleUnit!!.remainingTurnActions.remainingSteps).isEqualTo(2)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.BattleUnitMoved(battleUnitId, 0, 0, 2, 1)
        )
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
        val battleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(null)
        // When
        moveBattleUnit(battleUnitId = battleUnit.toDto().id, moveToRow = 1, moveToColumn = 0)
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
