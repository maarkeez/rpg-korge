package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanMove
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WhereShouldMoveTest {
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val whereCanMove: WhereCanMove = mock()
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val searchPosition: SearchPosition = mock()
    private val searchEnemyPlayer: SearchEnemyPlayer = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val healingNeed = HealingNeed(searchBattleUnitById, searchUnitById)
    private val searchBattlefield: SearchBattlefield = mock()
    private val searchTerrainById: SearchTerrainById = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val whereShouldMove =
        WhereShouldMove(
            searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
            whereCanMove = whereCanMove,
            searchBattleUnitById = searchBattleUnitById,
            searchPosition = searchPosition,
            searchEnemyPlayer = searchEnemyPlayer,
            searchUnitById = searchUnitById,
            healingNeed = healingNeed,
            searchBattlefield = searchBattlefield,
            searchTerrainById = searchTerrainById,
            searchEffectById = searchEffectById,
        )

    private val player = player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU)
    private val enemyPlayer = player(id = "player-2")
    private val unit = unit(id = "unit-1", healthPoints = 10)

    private fun battleUnitDto(
        id: String,
        playerId: String,
        remainingHealthPoints: Int,
        maxHealthPoints: Int = 10,
    ): BattleUnit.Dto =
        battleUnit(
            unit = unit(id = "unit-1", healthPoints = maxHealthPoints).toDto(),
            player = player(id = playerId).toDto(),
        ).toDto()
            .copy(id = id, remainingHealthPoints = remainingHealthPoints)

    private fun position(
        row: Int,
        column: Int,
    ) = Battlefield.Dto.PositionDto(row = row, column = column)

    private fun battlefield(positionToTerrainId: Map<Battlefield.Dto.PositionDto, String>) =
        Battlefield.Dto(
            rows = 1,
            columns = positionToTerrainId.size,
            tiles =
                positionToTerrainId
                    .map { (position, terrainId) ->
                        position to Battlefield.Dto.TileDto(battleUnitId = null, terrainId = terrainId)
                    }.toMap(),
        )

    private fun terrain(
        id: String,
        effect: Effect.Dto? = null,
    ) = Terrain.Dto(id = id, canBeOccupied = true, effectId = effect?.id)

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

    @Test
    fun `should move to the healing terrain when the battle unit has low health and other utilities are equivalent`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        val healingEffect = EffectMother.increaseHealthEffect(id = "effect-healing", healing = 10).toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(null)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        whenever(searchBattlefield()).thenReturn(
            battlefield(
                mapOf(
                    position(0, 0) to "neutral",
                    position(0, 1) to "healing",
                    position(0, 2) to "neutral",
                ),
            ),
        )
        whenever(searchTerrainById("healing")).thenReturn(terrain(id = "healing", effect = healingEffect))
        whenever(searchTerrainById("neutral")).thenReturn(terrain(id = "neutral"))
        whenever(searchEffectById(healingEffect.id)).thenReturn(healingEffect)
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 1))
    }

    @Test
    fun `should move to the neutral terrain when the battle unit has low health and stands on damaging terrain`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        val damagingEffect = EffectMother.decreaseHealthEffect(id = "effect-damaging", damage = 10).toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(null)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        whenever(searchBattlefield()).thenReturn(
            battlefield(
                mapOf(
                    position(0, 0) to "damaging",
                    position(0, 1) to "neutral",
                    position(0, 2) to "damaging",
                ),
            ),
        )
        whenever(searchTerrainById("damaging")).thenReturn(terrain(id = "damaging", effect = damagingEffect))
        whenever(searchTerrainById("neutral")).thenReturn(terrain(id = "neutral"))
        whenever(searchEffectById(damagingEffect.id)).thenReturn(damagingEffect)
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 1))
    }

    @Test
    fun `should move to the healing terrain when the battle unit has low health and candidates include neutral and damaging terrains`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        val healingEffect = EffectMother.increaseHealthEffect(id = "effect-healing", healing = 10).toDto()
        val damagingEffect = EffectMother.decreaseHealthEffect(id = "effect-damaging", damage = 10).toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(null)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2), position(0, 3)))
        whenever(searchBattlefield()).thenReturn(
            battlefield(
                mapOf(
                    position(0, 0) to "neutral",
                    position(0, 1) to "healing",
                    position(0, 2) to "neutral",
                    position(0, 3) to "damaging",
                ),
            ),
        )
        whenever(searchTerrainById("healing")).thenReturn(terrain(id = "healing", effect = healingEffect))
        whenever(searchTerrainById("neutral")).thenReturn(terrain(id = "neutral"))
        whenever(searchTerrainById("damaging")).thenReturn(terrain(id = "damaging", effect = damagingEffect))
        whenever(searchEffectById(healingEffect.id)).thenReturn(healingEffect)
        whenever(searchEffectById(damagingEffect.id)).thenReturn(damagingEffect)
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 1))
    }

    @Test
    fun `should move to the position favored by the existing utilities when the battle unit has high health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 90, maxHealthPoints = 100)
        val enemyBattleUnit = battleUnitDto(id = "enemy-battle-unit", playerId = "player-2", remainingHealthPoints = 10)
        val healingEffect = EffectMother.increaseHealthEffect(id = "effect-healing", healing = 10).toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchPosition("enemy-battle-unit")).thenReturn(position(0, 3))
        whenever(searchEnemyPlayer("player-1")).thenReturn(enemyPlayer.toDto())
        whenever(searchBattleUnitsByPlayerId("player-2")).thenReturn(listOf(enemyBattleUnit))
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        whenever(searchBattlefield()).thenReturn(
            battlefield(
                mapOf(
                    position(0, 0) to "neutral",
                    position(0, 1) to "healing",
                    position(0, 2) to "neutral",
                ),
            ),
        )
        whenever(searchTerrainById("healing")).thenReturn(terrain(id = "healing", effect = healingEffect))
        whenever(searchTerrainById("neutral")).thenReturn(terrain(id = "neutral"))
        whenever(searchEffectById(healingEffect.id)).thenReturn(healingEffect)
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 2))
    }

    @Test
    fun `should move to the stronger healing terrain when the battle unit has low health and other utilities are equivalent`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        val strongHealingEffect = EffectMother.increaseHealthEffect(id = "effect-strong-healing", healing = 10).toDto()
        val weakHealingEffect = EffectMother.increaseHealthEffect(id = "effect-weak-healing", healing = 2).toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(null)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        whenever(searchBattlefield()).thenReturn(
            battlefield(
                mapOf(
                    position(0, 0) to "neutral",
                    position(0, 1) to "strong-healing",
                    position(0, 2) to "weak-healing",
                ),
            ),
        )
        whenever(searchTerrainById("strong-healing")).thenReturn(terrain(id = "strong-healing", effect = strongHealingEffect))
        whenever(searchTerrainById("weak-healing")).thenReturn(terrain(id = "weak-healing", effect = weakHealingEffect))
        whenever(searchTerrainById("neutral")).thenReturn(terrain(id = "neutral"))
        whenever(searchEffectById(strongHealingEffect.id)).thenReturn(strongHealingEffect)
        whenever(searchEffectById(weakHealingEffect.id)).thenReturn(weakHealingEffect)
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 1))
    }

    @Test
    fun `should move to the less damaging terrain when the battle unit has low health and stands on more damaging terrain`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", playerId = "player-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        val strongDamagingEffect = EffectMother.decreaseHealthEffect(id = "effect-strong-damaging", damage = 10).toDto()
        val weakDamagingEffect = EffectMother.decreaseHealthEffect(id = "effect-weak-damaging", damage = 2).toDto()
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position(0, 0))
        whenever(searchEnemyPlayer("player-1")).thenReturn(null)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        whenever(whereCanMove("battle-unit-1")).thenReturn(listOf(position(0, 1), position(0, 2)))
        whenever(searchBattlefield()).thenReturn(
            battlefield(
                mapOf(
                    position(0, 0) to "strong-damaging",
                    position(0, 1) to "weak-damaging",
                    position(0, 2) to "strong-damaging",
                ),
            ),
        )
        whenever(searchTerrainById("strong-damaging")).thenReturn(terrain(id = "strong-damaging", effect = strongDamagingEffect))
        whenever(searchTerrainById("weak-damaging")).thenReturn(terrain(id = "weak-damaging", effect = weakDamagingEffect))
        whenever(searchEffectById(strongDamagingEffect.id)).thenReturn(strongDamagingEffect)
        whenever(searchEffectById(weakDamagingEffect.id)).thenReturn(weakDamagingEffect)
        // When
        val result = whereShouldMove("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(position(0, 1))
    }
}
