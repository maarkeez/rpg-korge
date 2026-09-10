package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnBattleUnitDeployedTest {
    private val eventBus = InMemoryEventBus()
    private val updateBattlefieldOccupancy: UpdateBattlefieldOccupancy = mock()
    private val onBattleUnitDeployed =
        OnBattleUnitDeployed(
            updateBattlefieldOccupancy = updateBattlefieldOccupancy,
            eventBus = eventBus,
        )

    @Test
    fun `should update battlefield occupancy when a battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val row = 1
        val column = 2
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = battleUnitId, row = row, column = column))
        // When
        eventBus.dispatch()
        // Then
        verify(updateBattlefieldOccupancy).invoke(row = row, column = column, battleUnitId = battleUnitId)
    }

    @Test
    fun `should not update battlefield occupancy when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "player-1", battleUnitId = "battle-unit-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(updateBattlefieldOccupancy, never()).invoke(any(), any(), any())
    }
}
