package screen.battlefieldHud.usecases.services

import battlefield.adapters.presentation.*
import battlefield.domain.*
import battlefield.usecases.queries.*
import battleunit.adapters.presentation.*
import battleunit.domain.*
import battleunit.usecases.queries.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto

class MovementServiceTest {
    private val battlefieldApi: BattlefieldApi = mock()
    private val battleUnitApi: BattleUnitApi = mock()
    private val searchTilesThatCanBeOccupied: SearchTilesThatCanBeOccupied = mock()
    private val canMoveTo: CanMoveTo = mock()
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val movementService = MovementService(
        battlefieldApi = battlefieldApi,
        battleUnitApi = battleUnitApi,
    )

    @Before
    fun setUp() {
        whenever(battlefieldApi.searchTilesThatCanBeOccupied).thenReturn(searchTilesThatCanBeOccupied)
        whenever(battleUnitApi.canMoveTo).thenReturn(canMoveTo)
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
    }

    @Test
    fun `should return only the tiles the battle unit can move to when computing the tiles where it can move`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(id = "battle-unit-1").toDto()
            .copy(
                remainingTurnActions = BattleUnit.Dto.RemainingTurnActionsDto(
                    remainingCasts = 1,
                    remainingSteps = 2,
                )
            )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchTilesThatCanBeOccupied("battle-unit-1", 2)).thenReturn(
            listOf(
                Battlefield.Dto.PositionDto(row = 0, column = 1),
                Battlefield.Dto.PositionDto(row = 1, column = 0),
                Battlefield.Dto.PositionDto(row = 0, column = 2),
            )
        )
        whenever(canMoveTo("battle-unit-1", 0, 1)).thenReturn(true)
        whenever(canMoveTo("battle-unit-1", 1, 0)).thenReturn(false)
        whenever(canMoveTo("battle-unit-1", 0, 2)).thenReturn(true)
        // When
        val tilesWhereCanMove = movementService.tilesWhereCanMove("battle-unit-1")
        // Then
        assertThat(tilesWhereCanMove).containsExactly(
            TileDto(row = 0, column = 1),
            TileDto(row = 0, column = 2),
        )
    }

    @Test
    fun `should return an empty set when there are no tiles that can be occupied`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(id = "battle-unit-1").toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchTilesThatCanBeOccupied("battle-unit-1", battleUnit.remainingTurnActions.remainingSteps))
            .thenReturn(emptyList())
        // When
        val tilesWhereCanMove = movementService.tilesWhereCanMove("battle-unit-1")
        // Then
        assertThat(tilesWhereCanMove).isEmpty()
    }
}
