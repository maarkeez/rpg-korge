package com.mkz.rpg.terrain.adapters.events

import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.terrain.usecases.commands.InitialiseAllowedTransitions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestAllowedTransitionsInitializationTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseAllowedTransitions: InitialiseAllowedTransitions = mock()
    private val onRequestAllowedTransitionsInitialization =
        OnRequestAllowedTransitionsInitialization(
            initialiseAllowedTransitions = initialiseAllowedTransitions,
            eventBus = eventBus,
        )

    @Test
    fun `should initialise the allowed transitions when the request is dispatched`() {
        // Given
        eventBus.publish(TerrainEvent.RequestAllowedTransitionsInitialization)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseAllowedTransitions).invoke()
    }
}
