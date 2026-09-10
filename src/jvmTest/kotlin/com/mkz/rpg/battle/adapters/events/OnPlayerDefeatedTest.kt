package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnPlayerDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val finishBattle: FinishBattle = mock()
    private val onPlayerDefeated =
        OnPlayerDefeated(
            eventBus = eventBus,
            finishBattle = finishBattle,
        )

    @Test
    fun `should finish battle when a player is defeated`() {
        // Given
        eventBus.publish(BattleEvent.PlayerDefeated(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(finishBattle).invoke()
    }

    @Test
    fun `should not finish battle when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(finishBattle, never()).invoke()
    }
}
