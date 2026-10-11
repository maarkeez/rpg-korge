package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseBattlefieldTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseBattlefield: InitialiseBattlefield = mock()
    private val onRequestInitialiseBattlefield = OnRequestInitialiseBattlefield(initialiseBattlefield, eventBus)

    @Test
    fun `should initialise the battlefield when the request is dispatched`() {
        // Given
        eventBus.publish(BattlefieldEvent.RequestInitialiseBattlefield)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseBattlefield).invoke()
    }
}
