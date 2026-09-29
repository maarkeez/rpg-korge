package com.mkz.rpg.terrain.adapters.events

import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.commands.InitialiseTerrains
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseTerrainsTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseTerrains: InitialiseTerrains = mock()
    private val onRequestInitialiseTerrains =
        OnRequestInitialiseTerrains(
            initialiseTerrains = initialiseTerrains,
            eventBus = eventBus,
        )

    @Test
    fun `should initialise the terrains when the request is dispatched`() {
        // Given
        eventBus.publish(TerrainEvent.RequestInitialiseTerrains)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseTerrains).invoke()
    }
}
