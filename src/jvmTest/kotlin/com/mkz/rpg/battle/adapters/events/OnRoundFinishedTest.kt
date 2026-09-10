package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnRoundFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val startNextRound: StartNextRound = mock()
    private val onRoundFinished =
        OnRoundFinished(
            eventBus = eventBus,
            startNextRound = startNextRound,
        )

    @Test
    fun `should start next round when a battle round is finished`() {
        // Given
        eventBus.publish(BattleEvent.BattleRoundFinished(round = 1))
        // When
        eventBus.dispatch()
        // Then
        verify(startNextRound).invoke()
    }

    @Test
    fun `should not start next round when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(startNextRound, never()).invoke()
    }
}
