package com.mkz.rpg.effect.adapters.events

import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.effect.usecases.commands.InitialiseEffects
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseEffectsTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseEffects: InitialiseEffects = mock()
    private val onRequestInitialiseEffects = OnRequestInitialiseEffects(initialiseEffects, eventBus)

    @Test
    fun `should initialise the effects when the request is dispatched`() {
        // Given
        eventBus.publish(EffectEvent.RequestInitialiseEffects)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseEffects).invoke()
    }
}
