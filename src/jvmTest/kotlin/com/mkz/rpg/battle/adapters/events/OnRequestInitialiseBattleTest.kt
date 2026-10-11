package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.InitialiseBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnRequestInitialiseBattleTest {
    private val eventBus = InMemoryEventBus()
    private val initialiseBattle: InitialiseBattle = mock()
    private val onRequestInitialiseBattle = OnRequestInitialiseBattle(initialiseBattle, eventBus)

    @Test
    fun `should initialise the battle when the request is dispatched`() {
        // Given
        eventBus.publish(BattleEvent.RequestInitialiseBattle)
        // When
        eventBus.dispatch()
        // Then
        verify(initialiseBattle).invoke()
    }
}
