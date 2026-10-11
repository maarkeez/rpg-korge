package com.mkz.rpg.ability.adapters.events

import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.usecases.commands.InitialiseAbilities
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseAbilitiesTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseAbilities: InitialiseAbilities = mock()
    private val onRequestInitialiseAbilities = OnRequestInitialiseAbilities(initialiseAbilities, eventBus)

    @Test
    fun `should initialise the abilities when the request is dispatched`() {
        // Given
        eventBus.publish(AbilityEvent.RequestInitialiseAbilities)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseAbilities).invoke()
    }
}
