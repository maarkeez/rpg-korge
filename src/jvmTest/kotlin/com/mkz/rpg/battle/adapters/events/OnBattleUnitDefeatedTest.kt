package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val defeatPlayer: DefeatPlayer = mock()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            eventBus = eventBus,
            defeatPlayer = defeatPlayer,
        )

    @Test
    fun `should defeat player when a battle unit is defeated`() {
        // Given
        val defeatedPlayer = "player-1"
        val defeatedBattleUnit = "battle-unit-1"
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(defeatedPlayer, defeatedBattleUnit))
        // When
        eventBus.dispatch()
        // Then
        verify(defeatPlayer).invoke(defeatedPlayer)
    }

    @Test
    fun `should not defeat player when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        verify(defeatPlayer, never()).invoke(any())
    }
}
