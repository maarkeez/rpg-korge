package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBrain.usecases.commands.PlayTurn
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnPlayerTurnStartedTest {
    private val eventBus = InMemoryEventBus()
    private val playTurn: PlayTurn = mock()
    private val onPlayerTurnStarted =
        OnPlayerTurnStarted(
            eventBus = eventBus,
            playTurn = playTurn,
        )

    @Test
    fun `should play turn when a player turn starts`() {
        // Given
        val playerId = "player-1"
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = playerId))
        // When
        eventBus.dispatch()
        // Then
        verify(playTurn).invoke(playerId)
    }

    @Test
    fun `should not play turn when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleEvent.PlayerDefeated(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(playTurn, never()).invoke(any())
    }
}
