package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldMother
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchDistancesFromHomeTest {
    private val queries = InMemoryQueries()
    private val searchDistancesFromHome = queries.searchDistancesFromHome

    init {
        queries.terrainRepository.create(terrainThatCanBeOccupied(id = "sand"))
        queries.terrainRepository.create(terrainThatCanBeOccupied(id = "lava", effectId = "effect-lava"))
        queries.terrainRepository.create(terrainThatCanBeOccupied(id = "water", effectId = "effect-water"))
        queries.effectRepository.create(damageEffect(id = "effect-lava", damage = 10))
        queries.effectRepository.create(healEffect(id = "effect-water", healing = 10))
    }

    private fun givenBattlefield(
        map: String,
        occupants: Map<String, Pair<Int, Int>> = emptyMap(),
    ) {
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

    private fun position(
        row: Int,
        column: Int,
    ) = PositionDto(row = row, column = column)

    @Test
    fun `should count the tiles walked from home to each tile`() {
        // Given
        givenBattlefield("sand sand sand")
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldContainExactly mapOf(position(0, 0) to 0, position(0, 1) to 1, position(0, 2) to 2)
    }

    @Test
    fun `should walk around the void`() {
        // Given
        givenBattlefield(
            """
            sand void sand
            sand sand sand
            """,
        )
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldContainExactly
            mapOf(
                position(0, 0) to 0,
                position(1, 0) to 1,
                position(1, 1) to 2,
                position(1, 2) to 3,
                position(0, 2) to 4,
            )
    }

    @Test
    fun `should walk around harmful terrain, which it never wanders to, but count it from the nearest walked tile`() {
        // Given
        givenBattlefield(
            """
            sand lava sand
            sand sand sand
            """,
        )
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldContainExactly
            mapOf(
                position(0, 0) to 0,
                position(0, 1) to 1,
                position(1, 0) to 1,
                position(1, 1) to 2,
                position(1, 2) to 3,
                position(0, 2) to 4,
            )
    }

    @Test
    fun `should walk over healing terrain`() {
        // Given
        givenBattlefield("sand water sand")
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldContainExactly mapOf(position(0, 0) to 0, position(0, 1) to 1, position(0, 2) to 2)
    }

    @Test
    fun `should walk through the battle units`() {
        // Given
        givenBattlefield("sand sand sand", occupants = mapOf("rat-1" to (0 to 0), "rat-2" to (0 to 1)))
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldContainExactly mapOf(position(0, 0) to 0, position(0, 1) to 1, position(0, 2) to 2)
    }

    @Test
    fun `should walk from home even when home is on harmful terrain`() {
        // Given
        givenBattlefield("lava sand sand")
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldContainExactly mapOf(position(0, 0) to 0, position(0, 1) to 1, position(0, 2) to 2)
    }

    @Test
    fun `should leave out the tiles that can only be walked to across void or harmful terrain`() {
        // Given
        givenBattlefield("sand void sand lava sand")
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 2)
        // Then
        distances shouldContainExactly mapOf(position(0, 2) to 0, position(0, 3) to 1)
    }

    @Test
    fun `should only count home when there is no battlefield`() {
        // When
        val distances = searchDistancesFromHome(homeRow = 0, homeColumn = 0)
        // Then
        distances shouldBe mapOf(position(0, 0) to 0)
    }
}
