package cpuBrain.usecases.queries

import battlefield.domain.*
import battlefield.usecases.queries.*
import battleunit.domain.*
import battleunit.usecases.queries.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import player.domain.*
import player.usecases.queries.*
import unit.domain.*
import unit.usecases.queries.*

class WhereShouldMoveTest {
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val whereCanMove: WhereCanMove = mock()
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val searchPosition: SearchPosition = mock()
    private val searchEnemyPlayer: SearchEnemyPlayer = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val whereShouldMove = WhereShouldMove(
        searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
        whereCanMove = whereCanMove,
        searchBattleUnitById = searchBattleUnitById,
        searchPosition = searchPosition,
        searchEnemyPlayer = searchEnemyPlayer,
        searchUnitById = searchUnitById,
    )

    private val player = PlayerMother.player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU)
    private val enemyPlayer = PlayerMother.player(id = "player-2")
    private val unit = UnitMother.unit(id = "unit-1", healthPoints = 10)

    private fun battleUnitDto(id: String, playerId: String, remainingHealthPoints: Int): BattleUnit.Dto =
        BattleUnitMother
            .battleUnit(unit = unit.toDto(), player = PlayerMother.player(id = playerId).toDto())
            .toDto()
            .copy(id = id, remainingHealthPoints = remainingHealthPoints)

    private fun position(row: Int, column: Int) = Battlefield.Dto.PositionDto(row = row, column = column)

    @Test
    fun `should move to the position closest to the enemy when the battle unit is healthy`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10)
        val enemyBattleUnit = battleUnitDto(id = "enemy-battle-unit", playerId = "player-2", remainingHealthPoints = 10)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit.toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchPosition("enemy-battle-unit")).thenReturn(position(0, 3))
        whenever(searchEnemyPlayer("player-1")).thenReturn(enemyPlayer.toDto())
        whenever(searchBattleUnitsByPlayerId("player-2")).thenReturn(listOf(enemyBattleUnit))
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 2))
    }

    @Test
    fun `should move to the position closest to the ally when the battle unit is hurt`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 1)
        val enemyBattleUnit = battleUnitDto(id = "enemy-battle-unit", playerId = "player-2", remainingHealthPoints = 10)
        val allyBattleUnit = battleUnitDto(id = "ally-battle-unit", playerId = "player-1", remainingHealthPoints = 10)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit.toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchPosition("enemy-battle-unit")).thenReturn(position(0, 3))
        whenever(searchPosition("ally-battle-unit")).thenReturn(position(0, 1))
        whenever(searchEnemyPlayer("player-1")).thenReturn(enemyPlayer.toDto())
        whenever(searchBattleUnitsByPlayerId("player-2")).thenReturn(listOf(enemyBattleUnit))
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(allyBattleUnit))
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 1))
    }

    @Test
    fun `should not move when the current position already offers the best utility`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10)
        val enemyBattleUnit = battleUnitDto(id = "enemy-battle-unit", playerId = "player-2", remainingHealthPoints = 10)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit.toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchPosition("enemy-battle-unit")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(enemyPlayer.toDto())
        whenever(searchBattleUnitsByPlayerId("player-2")).thenReturn(listOf(enemyBattleUnit))
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1)))
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `should not move when there are no enemies and no allies`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit.toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(null)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1)))
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isNull()
    }
}
