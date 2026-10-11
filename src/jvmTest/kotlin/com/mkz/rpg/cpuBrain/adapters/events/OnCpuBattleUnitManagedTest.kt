package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnCpuBattleUnitManagedTest {
    private val eventBus = InMemoryEventBus()
    private val manageCpuBattleUnit: ManageCpuBattleUnit = mock()
    private val listener = OnCpuBattleUnitManaged(manageCpuBattleUnit, eventBus)

    @Test
    fun `should manage the next cpu battle unit when a cpu battle unit is managed is dispatched`() {
        // Given
        eventBus.publish(CpuBrainEvent.CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(manageCpuBattleUnit).invoke(playerId = "cpu")
    }
}
