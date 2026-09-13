package com.mkz.rpg.usecases.commands

import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBrain.usecases.commands.PlayTurn
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
    private val playTurn =
        PlayTurn(
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

    private val player = player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU)

    private fun battleUnitDto(
        id: String,
        remainingCasts: Int,
    ): BattleUnit.Dto =
        battleUnit(
            unit = unit(abilities = listOf("ability-1")).toDto(),
            player = player.toDto(),
        ).toDto()
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
        whenever(searchPlayerById("player-1")).thenReturn(
            player(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN).toDto(),
        )
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
        val castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(row = 0, column = 0)))
        whenever(whereCanCast("battle-unit-1", "ability-1"))
            .thenReturn(listOf(castGroup))
        whenever(whereShouldMove("battle-unit-1")).thenReturn(null)
        // When
        playTurn("player-1")
        // Then
        verify(castAbility, atLeastOnce()).invoke("battle-unit-1", "ability-1", castGroup)
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
        verify(castAbility, never()).invoke(any(), any(), any())
    }
}
