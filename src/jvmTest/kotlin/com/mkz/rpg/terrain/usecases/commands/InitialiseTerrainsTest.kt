package com.mkz.rpg.terrain.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainAlreadyExists
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.services.TerrainLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseTerrainsTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()
    private val terrainLoader: TerrainLoader = mock()
    private val initialiseTerrains =
        InitialiseTerrains(
            terrainLoader = terrainLoader,
            terrainRepository = terrainRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create and store all the terrains when terrains are loaded`() {
        // Given
        whenever(terrainLoader.loadTerrains()).thenReturn(
            listOf(
                Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand", "void")),
                Terrain.Dto(id = "void", canBeOccupied = false, allowedTransitionTo = setOf("void", "sand")),
            ),
        )
        // When
        initialiseTerrains()
        // Then
        assertThat(terrainRepository.searchById("sand")?.toDto())
            .isEqualTo(Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand", "void")))
        assertThat(terrainRepository.searchById("void")?.toDto())
            .isEqualTo(Terrain.Dto(id = "void", canBeOccupied = false, allowedTransitionTo = setOf("void", "sand")))
    }

    @Test
    fun `should publish a terrain created event per terrain when the terrains are created`() {
        // Given
        whenever(terrainLoader.loadTerrains()).thenReturn(
            listOf(
                Terrain.Dto(id = "sand", canBeOccupied = true),
                Terrain.Dto(id = "void", canBeOccupied = false),
            ),
        )
        // When
        initialiseTerrains()
        // Then
        assertThat(eventBus)
            .hasPublishedEvents(
                TerrainEvent.TerrainCreated(terrainId = "sand"),
                TerrainEvent.TerrainCreated(terrainId = "void"),
            )
    }

    @Test
    fun `should fail when a terrain already exists`() {
        // Given
        terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true)))
        whenever(terrainLoader.loadTerrains()).thenReturn(
            listOf(Terrain.Dto(id = "sand", canBeOccupied = true)),
        )
        // When
        val result = runCatching { initialiseTerrains() }
        // Then
        assertThat(result.exceptionOrNull()).isInstanceOf(TerrainAlreadyExists::class.java)
    }

    @Test
    fun `should not create any terrain when no terrains are loaded`() {
        // Given
        whenever(terrainLoader.loadTerrains()).thenReturn(emptyList())
        // When
        initialiseTerrains()
        // Then
        assertThat(terrainRepository.searchById("sand")).isNull()
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
