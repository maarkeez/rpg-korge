package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnCpuBrainInitializedTest {
    private val eventBus = InMemoryEventBus()
    private val manageCpuBattleUnit: ManageCpuBattleUnit = mock()
    private val listener = OnCpuBrainInitialized(manageCpuBattleUnit, eventBus)

    @Test
    fun `should manage the next cpu battle unit when the cpu brain is initialized is dispatched`() {
        // Given
        eventBus.publish(CpuBrainEvent.CpuBrainInitialized(playerId = "cpu"))
        // When
        eventBus.dispatch()
        // Then
        verify(manageCpuBattleUnit).invoke(playerId = "cpu")
    }
}
