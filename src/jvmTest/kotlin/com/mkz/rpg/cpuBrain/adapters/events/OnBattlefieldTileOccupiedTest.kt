package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitMove
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnBattlefieldTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val resolveCpuBattleUnitMove: ResolveCpuBattleUnitMove = mock()
    private val listener = OnBattlefieldTileOccupied(resolveCpuBattleUnitMove, eventBus)

    @Test
    fun `should resolve the cpu battle unit move when a battlefield tile is occupied is dispatched`() {
        // Given
        eventBus.publish(BattlefieldEvent.BattlefieldTileOccupied(row = 2, column = 3, battlefieldUnitId = "unit-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(resolveCpuBattleUnitMove).invoke(battleUnitId = "unit-1", row = 2, column = 3)
    }
}
