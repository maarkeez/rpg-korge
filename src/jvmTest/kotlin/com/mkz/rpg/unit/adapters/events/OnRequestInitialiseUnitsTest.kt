package com.mkz.rpg.unit.adapters.events

import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.domain.UnitEvent
import com.mkz.rpg.unit.usecases.commands.InitialiseUnits
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseUnitsTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseUnits: InitialiseUnits = mock()
    private val onRequestInitialiseUnits = OnRequestInitialiseUnits(initialiseUnits, eventBus)

    @Test
    fun `should initialise the units when the request is dispatched`() {
        // Given
        eventBus.publish(UnitEvent.RequestInitialiseUnits)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseUnits).invoke()
    }
}
