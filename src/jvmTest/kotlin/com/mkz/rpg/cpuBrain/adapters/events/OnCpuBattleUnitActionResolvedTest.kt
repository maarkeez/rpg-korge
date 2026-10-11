package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnCpuBattleUnitActionResolvedTest {
    private val eventBus = InMemoryEventBus()
    private val manageCpuBattleUnit: ManageCpuBattleUnit = mock()
    private val listener = OnCpuBattleUnitActionResolved(manageCpuBattleUnit, eventBus)

    @Test
    fun `should manage the cpu battle unit again when its action is resolved is dispatched`() {
        // Given
        eventBus.publish(CpuBrainEvent.CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(manageCpuBattleUnit).invoke(playerId = "cpu")
    }
}
