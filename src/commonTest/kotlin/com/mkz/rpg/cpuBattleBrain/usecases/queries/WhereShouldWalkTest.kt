package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectMother.damageEffect
import com.mkz.rpg.effect.domain.EffectMother.healEffect
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WhereShouldWalkTest {
    private val queries = InMemoryQueries()
    private val whereShouldWalk = queries.whereShouldWalk

    init {
        queries.playerRepository.create(player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU))
        queries.playerRepository.create(player(id = "player-2"))
    }

    private fun givenBattleUnit(
        id: String,
        playerId: String,
        remainingHealth: Int,
        maxHealthPoints: Int = 10,
    ) {
        val unit = unit(id = "unit-1", baseMaximumHealth = maxHealthPoints, maximumSteps = 2).also(queries.unitRepository::create)
        queries.battleUnitRepository.create(battleUnit(id = id, unit = unit.toDto(), player = player(id = playerId).toDto(), remainingHealth = remainingHealth))
    }

    private fun givenTerrain(
        id: String,
        effect: Effect? = null,
    ) {
        effect?.let(queries.effectRepository::create)
        queries.terrainRepository.create(terrainThatCanBeOccupied(id = id, effectId = effect?.toDto()?.id))
    }

    private fun givenBattlefieldRow(
        terrainIds: List<String>,
        battleUnitColumns: Map<String, Int>,
    ) = givenBattlefield(listOf(terrainIds), battleUnitColumns.mapValues { (_, column) -> position(0, column) })

    private fun givenBattlefield(
        terrainIds: List<List<String>>,
        battleUnitPositions: Map<String, Battlefield.Dto.PositionDto>,
    ) {
        val emptyBattlefield =
            BattlefieldMother.battlefield(
                rows = terrainIds.size,
                columns = terrainIds.first().size,
                tiles = terrainIds,
                terrainTransitionRules = BattlefieldMother.terrainTransitionRules(terrainIds),
            )
        val battlefield =
            battleUnitPositions.entries.fold(emptyBattlefield) { battlefield, (battleUnitId, position) ->
                battlefield.occupy(position.row, position.column, battleUnitId).pullEvents().second
            }
        queries.battlefieldRepository.create(battlefield)
    }

    private fun position(
        row: Int,
        column: Int,
    ) = Battlefield.Dto.PositionDto(row = row, column = column)

    @Test
    fun `should walk to the tile closest to the enemy when the battle unit is healthy`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10)
        givenBattleUnit(id = "enemy-battle-unit", playerId = "player-2", remainingHealth = 10)
        givenBattlefieldRow(List(4) { "sand" }, battleUnitColumns = mapOf("battle-unit-1" to 0, "enemy-battle-unit" to 3))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 2)
    }

    @Test
    fun `should not walk when the only enemy is out of reach`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10)
        givenBattleUnit(id = "enemy-battle-unit", playerId = "player-2", remainingHealth = 10)
        givenBattlefieldRow(List(7) { "sand" }, battleUnitColumns = mapOf("battle-unit-1" to 0, "enemy-battle-unit" to 6))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `should walk to the tile closest to the ally when the battle unit is hurt and the enemy is out of reach`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 1)
        givenBattleUnit(id = "enemy-battle-unit", playerId = "player-2", remainingHealth = 10)
        givenBattleUnit(id = "ally-battle-unit", playerId = "player-1", remainingHealth = 10)
        givenBattlefieldRow(List(13) { "sand" }, battleUnitColumns = mapOf("enemy-battle-unit" to 0, "battle-unit-1" to 6, "ally-battle-unit" to 12))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 8)
    }

    @Test
    fun `should walk to the tile closest to the ally when the battle unit is hurt`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 1)
        givenBattleUnit(id = "enemy-battle-unit", playerId = "player-2", remainingHealth = 10)
        givenBattleUnit(id = "ally-battle-unit", playerId = "player-1", remainingHealth = 10)
        givenBattlefieldRow(List(5) { "sand" }, battleUnitColumns = mapOf("enemy-battle-unit" to 0, "battle-unit-1" to 2, "ally-battle-unit" to 4))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 3)
    }

    @Test
    fun `should not walk when the current tile already offers the best utility`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10)
        givenBattleUnit(id = "enemy-battle-unit", playerId = "player-2", remainingHealth = 10)
        givenBattlefieldRow(List(3) { "sand" }, battleUnitColumns = mapOf("battle-unit-1" to 1, "enemy-battle-unit" to 2))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `should not walk when there are no enemies and no allies`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10)
        givenBattlefieldRow(List(3) { "sand" }, battleUnitColumns = mapOf("battle-unit-1" to 0))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `should walk to the healing terrain when the battle unit has low health and other utilities are equivalent`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10, maxHealthPoints = 100)
        givenTerrain(id = "neutral")
        givenTerrain(id = "healing", effect = healEffect(id = "effect-healing", healing = 10))
        givenBattlefieldRow(listOf("neutral", "healing", "neutral"), battleUnitColumns = mapOf("battle-unit-1" to 0))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 1)
    }

    @Test
    fun `should walk to the neutral terrain when the battle unit has low health and stands on damaging terrain`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10, maxHealthPoints = 100)
        givenTerrain(id = "neutral")
        givenTerrain(id = "damaging", effect = damageEffect(id = "effect-damaging", damage = 10))
        givenBattlefieldRow(listOf("damaging", "neutral", "damaging"), battleUnitColumns = mapOf("battle-unit-1" to 0))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 1)
    }

    @Test
    fun `should walk to the healing terrain when the battle unit has low health and the options include neutral and damaging terrains`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10, maxHealthPoints = 100)
        givenTerrain(id = "neutral")
        givenTerrain(id = "healing", effect = healEffect(id = "effect-healing", healing = 10))
        givenTerrain(id = "damaging", effect = damageEffect(id = "effect-damaging", damage = 10))
        givenBattlefieldRow(listOf("neutral", "damaging", "healing", "neutral"), battleUnitColumns = mapOf("battle-unit-1" to 0))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 2)
    }

    @Test
    fun `should walk to the tile favored by the existing utilities when the battle unit has high health`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 90, maxHealthPoints = 100)
        givenBattleUnit(id = "enemy-battle-unit", playerId = "player-2", remainingHealth = 10)
        givenTerrain(id = "neutral")
        givenTerrain(id = "healing", effect = healEffect(id = "effect-healing", healing = 10))
        givenBattlefieldRow(listOf("neutral", "healing", "neutral", "neutral"), battleUnitColumns = mapOf("battle-unit-1" to 0, "enemy-battle-unit" to 3))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 2)
    }

    @Test
    fun `should walk to the stronger healing terrain when the battle unit has low health and other utilities are equivalent`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10, maxHealthPoints = 100)
        givenTerrain(id = "neutral")
        givenTerrain(id = "strong-healing", effect = healEffect(id = "effect-strong-healing", healing = 10))
        givenTerrain(id = "weak-healing", effect = healEffect(id = "effect-weak-healing", healing = 2))
        givenBattlefieldRow(listOf("neutral", "strong-healing", "weak-healing"), battleUnitColumns = mapOf("battle-unit-1" to 0))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 1)
    }

    @Test
    fun `should walk to the less damaging terrain when the battle unit has low health and stands on more damaging terrain`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10, maxHealthPoints = 100)
        givenTerrain(id = "strong-damaging", effect = damageEffect(id = "effect-strong-damaging", damage = 10))
        givenTerrain(id = "weak-damaging", effect = damageEffect(id = "effect-weak-damaging", damage = 2))
        givenBattlefieldRow(listOf("strong-damaging", "weak-damaging", "strong-damaging"), battleUnitColumns = mapOf("battle-unit-1" to 0))
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(0, 1)
    }

    @Test
    fun `should not walk when every option is the same tiles apart from the nearest enemy`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10)
        givenBattleUnit(id = "enemy-battle-unit-1", playerId = "player-2", remainingHealth = 10)
        givenBattleUnit(id = "enemy-battle-unit-2", playerId = "player-2", remainingHealth = 10)
        queries.battlefieldRepository.create(
            BattlefieldMother.battlefield(
                "E K . F",
                occupants = mapOf('K' to "battle-unit-1", 'E' to "enemy-battle-unit-1", 'F' to "enemy-battle-unit-2"),
            ),
        )
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `should not walk when every option is the same tiles apart from the nearest ally`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 1)
        givenBattleUnit(id = "ally-battle-unit-1", playerId = "player-1", remainingHealth = 10)
        givenBattleUnit(id = "ally-battle-unit-2", playerId = "player-1", remainingHealth = 10)
        queries.battlefieldRepository.create(
            BattlefieldMother.battlefield(
                "A K . B",
                occupants = mapOf('K' to "battle-unit-1", 'A' to "ally-battle-unit-1", 'B' to "ally-battle-unit-2"),
            ),
        )
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `should walk to the healing terrain when every option is the same tiles apart from the nearest enemy and the battle unit has low health`() {
        // Given
        givenBattleUnit(id = "battle-unit-1", playerId = "player-1", remainingHealth = 10, maxHealthPoints = 100)
        val enemyPositions = listOf(position(0, 0), position(0, 2), position(1, 0), position(1, 2), position(2, 0), position(2, 2))
        enemyPositions.indices.forEach { index -> givenBattleUnit(id = "enemy-battle-unit-$index", playerId = "player-2", remainingHealth = 10) }
        givenTerrain(id = "neutral")
        givenTerrain(id = "healing", effect = healEffect(id = "effect-healing", healing = 10))
        givenBattlefield(
            terrainIds =
                listOf(
                    listOf("neutral", "neutral", "neutral"),
                    listOf("neutral", "neutral", "neutral"),
                    listOf("neutral", "healing", "neutral"),
                ),
            battleUnitPositions =
                mapOf("battle-unit-1" to position(1, 1)) +
                    enemyPositions.mapIndexed { index, position -> "enemy-battle-unit-$index" to position },
        )
        // When
        val result = whereShouldWalk("battle-unit-1")
        // Then
        result shouldBe position(2, 1)
    }
}
