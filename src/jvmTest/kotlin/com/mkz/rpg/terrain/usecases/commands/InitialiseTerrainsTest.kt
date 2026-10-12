package com.mkz.rpg.terrain.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainAlreadyExists
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.services.TerrainLoader
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
        terrainRepository.searchById("sand")?.toDto() shouldBe Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand", "void"))
        terrainRepository.searchById("void")?.toDto() shouldBe Terrain.Dto(id = "void", canBeOccupied = false, allowedTransitionTo = setOf("void", "sand"))
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
        eventBus.shouldHavePublishedEvents(
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
        result.exceptionOrNull().shouldBeInstanceOf<TerrainAlreadyExists>()
    }

    @Test
    fun `should not create any terrain when no terrains are loaded`() {
        // Given
        whenever(terrainLoader.loadTerrains()).thenReturn(emptyList())
        // When
        initialiseTerrains()
        // Then
        terrainRepository.searchById("sand").shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
