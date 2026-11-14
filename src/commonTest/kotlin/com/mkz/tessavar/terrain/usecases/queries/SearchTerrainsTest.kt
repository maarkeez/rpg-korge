package com.mkz.tessavar.terrain.usecases.queries

import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.Terrain
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchTerrainsTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val searchTerrains = SearchTerrains(terrainRepository)

    @Test
    fun `should list every terrain in id order when there are terrains`() {
        // Given
        terrainRepository.create(terrainThatCanBeOccupied(id = "water"))
        terrainRepository.create(terrainThatCanBeOccupied(id = "lava"))
        terrainRepository.create(terrainThatCanBeOccupied(id = "sand"))
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
