package com.mkz.rpg.terrain.usecases.commands

import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainError.TerrainNotFound
import com.mkz.rpg.terrain.domain.TerrainMother
import com.mkz.rpg.terrain.usecases.services.TerrainTransitionsService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseAllowedTransitionsTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val terrainTransitionsService: TerrainTransitionsService = mock()
    private val initialiseAllowedTransitions =
        InitialiseAllowedTransitions(
            terrainRepository = terrainRepository,
            terrainTransitionsService = terrainTransitionsService,
        )

    @Test
    fun `should update the terrains with allowed transitions when transitions are found`() {
        // Given
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand"))
        terrainRepository.create(TerrainMother.nonOccupiableTerrain(id = "void"))
        whenever(terrainTransitionsService.searchTransitions()).thenReturn(mapOf("sand" to setOf("void")))
        // When
        initialiseAllowedTransitions()
        // Then
        assertThat(terrainRepository.searchById("sand")?.canTransitionTo("void")).isTrue()
        assertThat(terrainRepository.searchById("void")?.canTransitionTo("sand")).isTrue()
    }

    @Test
    fun `should update all the referenced terrains when there are multiple transitions`() {
        // Given
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand"))
        terrainRepository.create(TerrainMother.nonOccupiableTerrain(id = "void"))
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "forest"))
        whenever(
            terrainTransitionsService.searchTransitions(),
        ).thenReturn(mapOf("sand" to setOf("void", "forest"), "void" to setOf("forest")))
        // When
        initialiseAllowedTransitions()
        // Then
        assertThat(terrainRepository.searchById("sand")?.toDto()?.allowedTransitionTo).containsExactlyInAnyOrder("void", "forest")
        assertThat(terrainRepository.searchById("void")?.toDto()?.allowedTransitionTo).containsExactlyInAnyOrder("sand", "forest")
        assertThat(terrainRepository.searchById("forest")?.toDto()?.allowedTransitionTo).containsExactlyInAnyOrder("sand", "void")
    }

    @Test
    fun `should not update the terrains when no transitions are found`() {
        // Given
        val sandTerrain = TerrainMother.occupiableTerrain(id = "sand")
        terrainRepository.create(sandTerrain)
        whenever(terrainTransitionsService.searchTransitions()).thenReturn(emptyMap())
        // When
        initialiseAllowedTransitions()
        // Then
        assertThat(terrainRepository.searchById("sand")?.toDto()).isEqualTo(sandTerrain.toDto())
    }

    @Test
    fun `should fail when a referenced terrain does not exist`() {
        // Given
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand"))
        whenever(terrainTransitionsService.searchTransitions()).thenReturn(mapOf("sand" to setOf("void")))
        // When
        val result = runCatching { initialiseAllowedTransitions() }
        // Then
        assertThat(result.exceptionOrNull()).isInstanceOf(TerrainNotFound::class.java)
        assertThat(result.exceptionOrNull() as TerrainNotFound).extracting("terrainId").isEqualTo("void")
    }
}
