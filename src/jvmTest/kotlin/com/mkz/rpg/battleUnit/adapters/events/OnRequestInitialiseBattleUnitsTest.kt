package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseBattleUnitsTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseBattleUnits: InitialiseBattleUnits = mock()
    private val onRequestInitialiseBattleUnits = OnRequestInitialiseBattleUnits(initialiseBattleUnits, eventBus)

    @Test
    fun `should initialise the battle units when the request is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.RequestInitialiseBattleUnits)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseBattleUnits).invoke()
    }
}
