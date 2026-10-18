package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.terrain.domain.Terrain
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchDistancesFromHomeTest {
    private val queries = InMemoryQueries()
    private val searchDistancesFromHome = queries.searchDistancesFromHome

    init {
        queries.terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true)))
        queries.terrainRepository.create(Terrain.create(Terrain.Dto(id = "lava", canBeOccupied = true, effectId = "effect-lava")))
        queries.terrainRepository.create(Terrain.create(Terrain.Dto(id = "water", canBeOccupied = true, effectId = "effect-water")))
        queries.effectRepository.create(decreaseHealthEffect(id = "effect-lava", damage = 10))
        queries.effectRepository.create(increaseHealthEffect(id = "effect-water", healing = 10))
    }

    // Stores a battlefield with one line per row and one terrain per tile ("sand", "lava", "water" or "void"), and the
    // battle units standing on the given tiles.
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
