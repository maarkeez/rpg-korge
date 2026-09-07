package cpuBrain.usecases.commands

import battle.usecases.commands.FinishPlayerTurn
import battlefield.domain.Battlefield
import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitMother
import battleunit.usecases.commands.CastAbility
import battleunit.usecases.commands.MoveBattleUnit
import battleunit.usecases.queries.CanCastAbility
import battleunit.usecases.queries.SearchBattleUnitById
import battleunit.usecases.queries.SearchBattleUnitsByPlayerId
import battleunit.usecases.queries.WhereCanCast
import cpuBrain.usecases.queries.WhereShouldMove
import org.junit.Test
import org.mockito.kotlin.*
import player.domain.PlayerMother
import player.usecases.queries.SearchPlayerById
import unit.domain.UnitMother

class PlayTurnTest {
    private val searchPlayerById: SearchPlayerById = mock()
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val moveBattleUnit: MoveBattleUnit = mock()
    private val whereCanCast: WhereCanCast = mock()
    private val canCastAbility: CanCastAbility = mock()
    private val castAbility: CastAbility = mock()
    private val finishPlayerTurn: FinishPlayerTurn = mock()
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val whereShouldMove: WhereShouldMove = mock()
    private val playTurn = PlayTurn(
        searchPlayerById = searchPlayerById,
        searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
        moveBattleUnit = moveBattleUnit,
        whereCanCast = whereCanCast,
        canCastAbility = canCastAbility,
        castAbility = castAbility,
        finishPlayerTurn = finishPlayerTurn,
        searchBattleUnitById = searchBattleUnitById,
        whereShouldMove = whereShouldMove,
    )

    private val player = PlayerMother.player(id = "player-1", type = "CPU")

    private fun battleUnitDto(id: String, remainingCasts: Int): BattleUnit.Dto =
        BattleUnitMother
            .battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1")).toDto(), player = player.toDto())
            .toDto()
            .copy(
                id = id,
                remainingTurnActions = BattleUnit.Dto.RemainingTurnActionsDto(remainingCasts = remainingCasts, remainingSteps = 3),
            )

    @Test
    fun `should not play turn when the player does not exist`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(null)
        // When
        playTurn("player-1")
        // Then
        verify(searchBattleUnitsByPlayerId, never()).invoke(any())
        verify(finishPlayerTurn, never()).invoke()
    }

    @Test
    fun `should not play turn when the player is not a cpu`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(PlayerMother.player(id = "player-1", type = "HUMAN").toDto())
        // When
        playTurn("player-1")
        // Then
        verify(searchBattleUnitsByPlayerId, never()).invoke(any())
        verify(finishPlayerTurn, never()).invoke()
    }

    @Test
    fun `should finish the player turn when the player is a cpu`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(player.toDto())
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        // When
        playTurn("player-1")
        // Then
        verify(finishPlayerTurn).invoke()
    }

    @Test
    fun `should cast ability when the battle unit can cast it`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(player.toDto())
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingCasts = 1)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit))
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(canCastAbility("battle-unit-1", "ability-1")).thenReturn(true)
        whenever(whereCanCast("battle-unit-1", "ability-1"))
            .thenReturn(listOf(WhereCanCast.PositionDto(row = 0, column = 0)))
        whenever(whereShouldMove("battle-unit-1")).thenReturn(null)
        // When
        playTurn("player-1")
        // Then
        verify(castAbility, atLeastOnce()).invoke("battle-unit-1", "ability-1", 0, 0)
    }

    @Test
    fun `should move battle unit when where should move returns a position`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(player.toDto())
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingCasts = 0)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit))
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(whereShouldMove("battle-unit-1")).thenReturn(Battlefield.Dto.PositionDto(row = 1, column = 1))
        // When
        playTurn("player-1")
        // Then
        verify(moveBattleUnit).invoke("battle-unit-1", 1, 1)
    }

    @Test
    fun `should not cast ability when the battle unit cannot cast it`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(player.toDto())
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingCasts = 1)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit))
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(canCastAbility("battle-unit-1", "ability-1")).thenReturn(false)
        whenever(whereShouldMove("battle-unit-1")).thenReturn(null)
        // When
        playTurn("player-1")
        // Then
        verify(castAbility, never()).invoke(any(), any(), any(), any())
    }
}
