package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchTerrainsTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val searchTerrains = SearchTerrains(terrainRepository)

    @Test
    fun `should list every terrain in id order when there are terrains`() {
        // Given
        terrainRepository.create(occupiableTerrain(id = "water"))
        terrainRepository.create(occupiableTerrain(id = "lava"))
        terrainRepository.create(occupiableTerrain(id = "sand"))
        // When
        val terrains = searchTerrains()
        // Then
        terrains.map(Terrain.Dto::id).shouldContainExactly("lava", "sand", "water")
    }

    @Test
    fun `should list nothing when there are no terrains`() {
        // When
        val terrains = searchTerrains()
        // Then
        terrains.shouldBeEmpty()
    }
}
