package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battlefield.domain.BattlefieldMother
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class IsOutsideTerritoryTest {
    private val queries = InMemoryQueries()
    private val isOutsideTerritory = queries.isOutsideTerritory

    init {
        queries.terrainRepository.create(terrainThatCanBeOccupied(id = "sand"))
        queries.terrainRepository.create(terrainThatCanBeOccupied(id = "lava", effectId = "effect-lava"))
        queries.effectRepository.create(damageEffect(id = "effect-lava", damage = 10))
    }

    private fun givenBattlefield(
        map: String,
        occupants: Map<String, Pair<Int, Int>>,
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

    @Test
    fun `should be outside its territory when it stands further than the wander distance from home`() {
        // Given
        givenBattlefield("sand sand sand sand sand", occupants = mapOf("rat-1" to (0 to 4)))
        // When / Then
        isOutsideTerritory(battleUnitId = "rat-1", distancesFromHome = queries.searchDistancesFromHome(homeRow = 0, homeColumn = 0)).shouldBeTrue()
    }

    @Test
    fun `should be inside its territory when it stands at its edge`() {
        // Given
        givenBattlefield("sand sand sand sand sand", occupants = mapOf("rat-1" to (0 to 3)))
        // When / Then
        isOutsideTerritory(battleUnitId = "rat-1", distancesFromHome = queries.searchDistancesFromHome(homeRow = 0, homeColumn = 0)).shouldBeFalse()
    }

    @Test
    fun `should count the distance from home around harmful terrain`() {
        // Given
        givenBattlefield(
            """
            sand lava sand
            sand sand sand
            """,
            occupants = mapOf("rat-1" to (0 to 2)),
        )
        // When / Then
        isOutsideTerritory(battleUnitId = "rat-1", distancesFromHome = queries.searchDistancesFromHome(homeRow = 0, homeColumn = 0)).shouldBeTrue()
    }

    @Test
    fun `should not be outside its territory when home can not be walked to`() {
        // Given
        givenBattlefield("sand void sand sand sand sand", occupants = mapOf("rat-1" to (0 to 5)))
        // When / Then
        isOutsideTerritory(battleUnitId = "rat-1", distancesFromHome = queries.searchDistancesFromHome(homeRow = 0, homeColumn = 0)).shouldBeFalse()
    }

    @Test
    fun `should not be outside its territory when it is not on the battlefield`() {
        // Given
        givenBattlefield("sand sand sand sand sand", occupants = emptyMap())
        // When / Then
        isOutsideTerritory(battleUnitId = "rat-1", distancesFromHome = queries.searchDistancesFromHome(homeRow = 0, homeColumn = 0)).shouldBeFalse()
    }
}
