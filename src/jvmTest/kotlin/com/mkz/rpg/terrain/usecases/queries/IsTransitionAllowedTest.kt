package com.mkz.rpg.terrain.usecases.queries

import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IsTransitionAllowedTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val isTransitionAllowed = IsTransitionAllowed(terrainRepository = terrainRepository)

    @Test
    fun `should be true when the terrain allows the transition`() {
        // Given
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("void")))
        // When
        val isAllowed = isTransitionAllowed(terrainId = "sand", transitionToTerrainId = "void")
        // Then
        assertThat(isAllowed).isTrue()
    }

    @Test
    fun `should be false when the terrain does not allow the transition`() {
        // Given
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("forest")))
        // When
        val isAllowed = isTransitionAllowed(terrainId = "sand", transitionToTerrainId = "void")
        // Then
        assertThat(isAllowed).isFalse()
    }

    @Test
    fun `should be false when the terrain does not exist`() {
        // When
        val isAllowed = isTransitionAllowed(terrainId = "sand", transitionToTerrainId = "void")
        // Then
        assertThat(isAllowed).isFalse()
    }
}
