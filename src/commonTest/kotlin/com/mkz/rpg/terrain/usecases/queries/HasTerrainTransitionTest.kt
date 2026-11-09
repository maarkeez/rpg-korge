package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class HasTerrainTransitionTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val hasTerrainTransition = HasTerrainTransition(terrainRepository = terrainRepository)

    @Test
    fun `should be true when the terrain has the transition`() {
        // Given
        terrainRepository.create(TerrainMother.terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("void")))
        // When
        val hasTransition = hasTerrainTransition(terrainId = "sand", toTerrainId = "void")
        // Then
        hasTransition.shouldBeTrue()
    }

    @Test
    fun `should be false when the terrain does not have the transition`() {
        // Given
        terrainRepository.create(TerrainMother.terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("forest")))
        // When
        val hasTransition = hasTerrainTransition(terrainId = "sand", toTerrainId = "void")
        // Then
        hasTransition.shouldBeFalse()
    }

    @Test
    fun `should be false when the terrain does not exist`() {
        // When
        val hasTransition = hasTerrainTransition(terrainId = "sand", toTerrainId = "void")
        // Then
        hasTransition.shouldBeFalse()
    }
}
