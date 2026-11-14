package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldMother
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WhereShouldWanderTest {
    private fun whereShouldWander(
        vararg rolls: Double,
        battleUnitId: String = "rat-1",
        homeRow: Int,
        homeColumn: Int,
        isReturning: Boolean = false,
    ): Battlefield.Dto.PositionDto? {
        val queries = queries(*rolls)
        val distancesFromHome = queries.searchDistancesFromHome(homeRow = homeRow, homeColumn = homeColumn)
        return queries.whereShouldWander(battleUnitId = battleUnitId, distancesFromHome = distancesFromHome, isReturning = isReturning)
    }

    private val givens = mutableListOf<(InMemoryQueries) -> Unit>()

    private fun queries(vararg rolls: Double): InMemoryQueries {
        val queries = InMemoryQueries(random = FixedDoublesRandom(*rolls))
        queries.playerRepository.create(player(id = "player-2", type = Player.Dto.PlayerTypeDto.CPU))
        terrains().forEach(queries.terrainRepository::create)
        queries.effectRepository.create(damageEffect(id = "effect-lava", damage = 10))
        queries.effectRepository.create(healEffect(id = "effect-water", healing = 10))
        givens.forEach { given -> given(queries) }
        return queries
    }

    private fun terrains() =
        listOf(
            terrainThatCanBeOccupied(id = "sand"),
            terrainThatCanBeOccupied(id = "lava", effectId = "effect-lava"),
            terrainThatCanBeOccupied(id = "water", effectId = "effect-water"),
        )

    private fun givenBattleUnits(
        vararg ids: String,
        remainingHealth: Int = 10,
    ) {
        givens += { queries ->
            val unit = unit(id = "unit-rat", baseMaximumHealth = 10).also(queries.unitRepository::create)
            ids.forEach { id ->
                queries.battleUnitRepository.create(battleUnit(id = id, unit = unit.toDto(), player = player(id = "player-2").toDto(), remainingHealth = remainingHealth))
            }
        }
    }

    private fun givenBattlefield(
        map: String,
        occupants: Map<String, Pair<Int, Int>>,
    ) {
        givens += { queries ->
            val tiles = map.trimIndent().lines().map { line -> line.split(" ").filter(String::isNotBlank) }
            val emptyBattlefield =
                BattlefieldMother.battlefield(
                    rows = tiles.size,
                    columns = tiles.first().size,
                    tiles = tiles,
                    terrainTransitionRules = BattlefieldMother.terrainTransitionRules(tiles),
                )
            val battlefield =
                occupants.entries.fold(emptyBattlefield) { battlefield, (battleUnitId, tile) ->
                    battlefield.occupy(tile.first, tile.second, battleUnitId).pullEvents().second
                }
            queries.battlefieldRepository.create(battlefield)
        }
    }

    private fun position(
        row: Int,
        column: Int,
    ) = Battlefield.Dto.PositionDto(row = row, column = column)

    @Test
    fun `should rest when the rest roll is below the rest chance`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield(
            """
            sand sand sand
            sand sand sand
            """,
            occupants = mapOf("rat-1" to (0 to 1)),
        )
        // When
        val step = whereShouldWander(0.49, 0.0, homeRow = 0, homeColumn = 1)
        // Then
        step.shouldBeNull()
    }

    @Test
    fun `should walk away from home in any direction when it stands at home`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield(
            """
            sand sand sand
            sand sand sand
            sand sand sand
            """,
            occupants = mapOf("rat-1" to (1 to 1)),
        )
        // When
        val northStep = whereShouldWander(0.5, 0.0, homeRow = 1, homeColumn = 1)
        val eastStep = whereShouldWander(0.5, 0.26, homeRow = 1, homeColumn = 1)
        val southStep = whereShouldWander(0.5, 0.51, homeRow = 1, homeColumn = 1)
        val westStep = whereShouldWander(0.5, 0.99, homeRow = 1, homeColumn = 1)
        // Then
        northStep shouldBe position(0, 1)
        eastStep shouldBe position(1, 2)
        southStep shouldBe position(2, 1)
        westStep shouldBe position(1, 0)
    }

    @Test
    fun `should walk back home a third of the times when it stands next to home`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand", occupants = mapOf("rat-1" to (0 to 1)))
        // When
        val awayFromHome = whereShouldWander(0.5, 0.65, homeRow = 0, homeColumn = 0)
        val backHome = whereShouldWander(0.5, 0.68, homeRow = 0, homeColumn = 0)
        // Then
        awayFromHome shouldBe position(0, 2)
        backHome shouldBe position(0, 0)
    }

    @Test
    fun `should only walk back when it stands at the edge of its territory`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand sand", occupants = mapOf("rat-1" to (0 to 3)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step shouldBe position(0, 2)
    }

    @Test
    fun `should still rest when it stands at the edge of its territory`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand sand", occupants = mapOf("rat-1" to (0 to 3)))
        // When
        val step = whereShouldWander(0.0, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step.shouldBeNull()
    }

    @Test
    fun `should walk back towards home when it stands outside its territory`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand sand sand", occupants = mapOf("rat-1" to (0 to 4)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step shouldBe position(0, 3)
    }

    @Test
    fun `should never rest when it stands outside its territory`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand sand sand", occupants = mapOf("rat-1" to (0 to 5)))
        // When
        val step = whereShouldWander(0.0, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step shouldBe position(0, 4)
    }

    @Test
    fun `should step to any tile nearer to home when it stands outside its territory`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield(
            """
            sand sand sand sand sand sand
            sand sand sand sand sand sand
            sand sand sand sand sand sand
            """,
            occupants = mapOf("rat-1" to (2 to 5)),
        )
        // When
        val northStep = whereShouldWander(0.0, homeRow = 0, homeColumn = 0)
        val westStep = whereShouldWander(0.99, homeRow = 0, homeColumn = 0)
        // Then
        northStep shouldBe position(1, 5)
        westStep shouldBe position(2, 4)
    }

    @Test
    fun `should keep heading home without resting while it returns, within its territory`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand", occupants = mapOf("rat-1" to (0 to 2)))
        // When
        val step = whereShouldWander(0.0, homeRow = 0, homeColumn = 0, isReturning = true)
        // Then
        step shouldBe position(0, 1)
    }

    @Test
    fun `should only step nearer to home while it returns`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand", occupants = mapOf("rat-1" to (0 to 1)))
        // When
        val step = whereShouldWander(0.99, homeRow = 0, homeColumn = 0, isReturning = true)
        // Then
        step shouldBe position(0, 0)
    }

    @Test
    fun `should wander again once it is back home after returning`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand", occupants = mapOf("rat-1" to (0 to 0)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 0, isReturning = true)
        // Then
        step shouldBe position(0, 1)
    }

    @Test
    fun `should walk around the void on its way home`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield(
            """
            sand void void sand sand
            sand sand sand sand sand
            """,
            occupants = mapOf("rat-1" to (0 to 4)),
        )
        // When
        val southStep = whereShouldWander(0.0, homeRow = 0, homeColumn = 0)
        val westStep = whereShouldWander(0.99, homeRow = 0, homeColumn = 0)
        // Then
        southStep shouldBe position(1, 4)
        westStep shouldBe position(0, 3)
    }

    @Test
    fun `should walk around harmful terrain on its way home`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield(
            """
            sand sand sand sand sand
            sand sand sand sand sand
            sand sand sand sand sand
            sand lava lava lava sand
            sand sand sand sand sand
            """,
            occupants = mapOf("rat-1" to (4 to 2)),
        )
        // When
        val eastStep = whereShouldWander(0.0, homeRow = 0, homeColumn = 2)
        val westStep = whereShouldWander(0.99, homeRow = 0, homeColumn = 2)
        // Then
        eastStep shouldBe position(4, 3)
        westStep shouldBe position(4, 1)
    }

    @Test
    fun `should head home when it stands on harmful terrain away from home`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand sand sand sand lava sand", occupants = mapOf("rat-1" to (0 to 5)))
        // When
        val step = whereShouldWander(0.99, homeRow = 0, homeColumn = 0)
        // Then
        step shouldBe position(0, 4)
    }

    @Test
    fun `should count its distance from home through the nearest tile next to it when it stands on harmful terrain`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield(
            """
            sand sand sand sand lava sand
            sand sand sand sand sand sand
            """,
            occupants = mapOf("rat-1" to (0 to 4)),
        )
        // When
        val step = whereShouldWander(0.0, homeRow = 0, homeColumn = 0)
        // Then
        step shouldBe position(0, 3)
    }

    @Test
    fun `should stay when home can only be walked to across harmful terrain`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand lava sand sand", occupants = mapOf("rat-1" to (0 to 2)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step.shouldBeNull()
    }

    @Test
    fun `should stay when home can not be walked to`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand void sand sand", occupants = mapOf("rat-1" to (0 to 3)))
        // When
        val step = whereShouldWander(0.0, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step.shouldBeNull()
    }

    @Test
    fun `should count the distance from home through the battle units`() {
        // Given
        givenBattleUnits("rat-1", "rat-2")
        givenBattlefield("sand sand sand sand", occupants = mapOf("rat-2" to (0 to 1), "rat-1" to (0 to 2)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step shouldBe position(0, 3)
    }

    @Test
    fun `should not step on a tile occupied by another battle unit`() {
        // Given
        givenBattleUnits("rat-1", "rat-2")
        givenBattlefield("sand sand sand", occupants = mapOf("rat-1" to (0 to 1), "rat-2" to (0 to 2)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 1)
        // Then
        step shouldBe position(0, 0)
    }

    @Test
    fun `should stay when it has nowhere to step to`() {
        // Given
        givenBattleUnits("rat-1", "rat-2")
        givenBattlefield("void sand sand", occupants = mapOf("rat-1" to (0 to 1), "rat-2" to (0 to 2)))
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 1)
        // Then
        step.shouldBeNull()
    }

    @Test
    fun `should never step on harmful terrain`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("lava sand sand", occupants = mapOf("rat-1" to (0 to 1)))
        // When
        val step = whereShouldWander(0.5, 0.99, homeRow = 0, homeColumn = 1)
        // Then
        step shouldBe position(0, 2)
    }

    @Test
    fun `should not rest when it stands on harmful terrain`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand lava sand", occupants = mapOf("rat-1" to (0 to 1)))
        // When
        val step = whereShouldWander(0.0, 0.0, homeRow = 0, homeColumn = 1)
        // Then
        step shouldBe position(0, 2)
    }

    @Test
    fun `should prefer healing terrain when it is hurt`() {
        // Given
        givenBattleUnits("rat-1", remainingHealth = 5)
        givenBattlefield("water sand sand", occupants = mapOf("rat-1" to (0 to 1)))
        // When
        val step = whereShouldWander(0.5, 0.41, homeRow = 0, homeColumn = 1)
        // Then
        step shouldBe position(0, 0)
    }

    @Test
    fun `should not mind healing terrain when it is healthy`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("water sand sand", occupants = mapOf("rat-1" to (0 to 1)))
        // When
        val step = whereShouldWander(0.5, 0.41, homeRow = 0, homeColumn = 1)
        // Then
        step shouldBe position(0, 2)
    }

    @Test
    fun `should stay when the battle unit is not on the battlefield`() {
        // Given
        givenBattleUnits("rat-1")
        givenBattlefield("sand sand", occupants = emptyMap())
        // When
        val step = whereShouldWander(0.5, 0.0, homeRow = 0, homeColumn = 0)
        // Then
        step.shouldBeNull()
    }
}
