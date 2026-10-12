package com.mkz.rpg.terrain.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainAlreadyExists
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.services.FakeTerrainLoader
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class InitialiseTerrainsTest {
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should create and store all the terrains when terrains are loaded`() {
        // Given
        val initialiseTerrains =
            InitialiseTerrains(
                FakeTerrainLoader(
                    terrains =
                        listOf(
                            Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand", "void")),
                            Terrain.Dto(id = "void", canBeOccupied = false, allowedTransitionTo = setOf("void", "sand")),
                        ),
                ),
                terrainRepository,
                eventBus,
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
        val initialiseTerrains =
            InitialiseTerrains(
                FakeTerrainLoader(
                    terrains =
                        listOf(
                            Terrain.Dto(id = "sand", canBeOccupied = true),
                            Terrain.Dto(id = "void", canBeOccupied = false),
                        ),
                ),
                terrainRepository,
                eventBus,
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
        val initialiseTerrains = InitialiseTerrains(FakeTerrainLoader(terrains = listOf(Terrain.Dto(id = "sand", canBeOccupied = true))), terrainRepository, eventBus)
        // When
        val result = runCatching { initialiseTerrains() }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<TerrainAlreadyExists>()
    }

    @Test
    fun `should not create any terrain when no terrains are loaded`() {
        // Given
        val initialiseTerrains = InitialiseTerrains(FakeTerrainLoader(terrains = emptyList()), terrainRepository, eventBus)
        // When
        initialiseTerrains()
        // Then
        terrainRepository.searchById("sand").shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
