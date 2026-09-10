package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val updateBattlefieldOccupancy: UpdateBattlefieldOccupancy = mock()
    private val onBattleUnitMoved =
        OnBattleUnitMoved(
            updateBattlefieldOccupancy = updateBattlefieldOccupancy,
            eventBus = eventBus,
        )

    @Test
    fun `should update battlefield occupancy when a battle unit is moved`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val toRow = 1
        val toColumn = 2
        eventBus.publish(
            BattleUnitEvent.BattleUnitMoved(
                battleUnitId = battleUnitId,
                fromRow = 0,
                fromColumn = 0,
                toRow = toRow,
                toColumn = toColumn,
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        verify(updateBattlefieldOccupancy).invoke(row = toRow, column = toColumn, battleUnitId = battleUnitId)
    }

    @Test
    fun `should not update battlefield occupancy when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        verify(updateBattlefieldOccupancy, never()).invoke(any(), any(), any())
    }
}
