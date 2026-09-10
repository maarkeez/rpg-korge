package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val removeOccupant: RemoveOccupant = mock()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            removeOccupant = removeOccupant,
            eventBus = eventBus,
        )

    @Test
    fun `should remove occupant when a battle unit is defeated`() {
        // Given
        val battleUnitId = "battle-unit-1"
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "player-1", battleUnitId = battleUnitId))
        // When
        eventBus.dispatch()
        // Then
        verify(removeOccupant).invoke(battleUnitId = battleUnitId)
    }

    @Test
    fun `should not remove occupant when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        verify(removeOccupant, never()).invoke(any())
    }
}
