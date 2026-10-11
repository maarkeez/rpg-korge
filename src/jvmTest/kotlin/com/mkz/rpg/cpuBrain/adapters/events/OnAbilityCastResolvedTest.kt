package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnAbilityCastResolvedTest {
    private val eventBus = InMemoryEventBus()
    private val resolveCpuBattleUnitCast: ResolveCpuBattleUnitCast = mock()
    private val listener = OnAbilityCastResolved(resolveCpuBattleUnitCast, eventBus)

    @Test
    fun `should resolve the cpu battle unit cast when an ability cast is resolved is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.AbilityCastResolved(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        eventBus.dispatch()
        // Then
        verify(resolveCpuBattleUnitCast).invoke(battleUnitId = "unit-1", abilityId = "sword")
    }
}
