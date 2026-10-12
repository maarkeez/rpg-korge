package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuBrain.domain.CpuBrain
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class OnAbilityCastResolvedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val listener = OnAbilityCastResolved(ResolveCpuBattleUnitCast(queries.cpuBrainRepository, eventBus), eventBus)

    @Test
    fun `should resolve the cpu battle unit cast when an ability cast is resolved is dispatched`() {
        // Given
        queries.cpuBrainRepository.save(
            CpuBrain
                .startTurn(playerId = "cpu", battleUnitIds = listOf("unit-1"))
                .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                .pullEvents()
                .second,
        )
        eventBus.publish(BattleUnitEvent.AbilityCastResolved(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        eventBus.dispatch()
        // Then
        queries.cpuBrainRepository
            .search()!!
            .toDto()
            .isAwaitingAction
            .shouldBeFalse()
    }
}
