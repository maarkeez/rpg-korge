package com.mkz.rpg.player.adapters.events

import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.usecases.commands.InitialisePlayers
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialisePlayersTest {
    private val eventBus = InMemoryEventBus()
    private val initialisePlayers: InitialisePlayers = mock()
    private val onRequestInitialisePlayers = OnRequestInitialisePlayers(initialisePlayers, eventBus)

    @Test
    fun `should initialise the players when the request is dispatched`() {
        // Given
        eventBus.publish(PlayerEvent.RequestInitialisePlayers)
        // When
        eventBus.dispatch()
        // Then
        verify(initialisePlayers).invoke()
    }
}
