package com.mkz.rpg.terrain.adapters.events

import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.rpg.terrain.usecases.commands.InitialiseTerrains
import com.mkz.rpg.terrain.usecases.services.FakeTerrainLoader
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseTerrainsTest {
    private val eventBus = InMemoryEventBus()
    private val terrainRepository = InMemoryTerrainRepository()
    private val sand = terrainThatCanBeOccupied(id = "sand").toDto()
    private val onRequestInitialiseTerrains =
        OnRequestInitialiseTerrains(
            initialiseTerrains = InitialiseTerrains(FakeTerrainLoader(terrains = listOf(sand)), terrainRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should initialise the terrains when the request is dispatched`() {
        // Given
        eventBus.publish(TerrainEvent.RequestInitialiseTerrains)
        // When
        eventBus.dispatch()
        // Then
        terrainRepository.searchById("sand")?.toDto() shouldBe sand
    }
}
