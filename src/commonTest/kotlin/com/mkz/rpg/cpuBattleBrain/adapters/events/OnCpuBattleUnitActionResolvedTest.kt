package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class OnCpuBattleUnitActionResolvedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val manageCpuBattleUnit =
        ManageCpuBattleUnit(queries.cpuBattleBrainRepository, queries.searchBattleUnitsByPlayerId, queries.whatShouldCast, queries.whereShouldMove, eventBus)
    private val listener = OnCpuBattleUnitActionResolved(manageCpuBattleUnit, eventBus)

    @Test
    fun `should manage the cpu battle unit again when its action is resolved is dispatched`() {
        // Given
        // The rat has been defeated, so it has nothing left to do this turn.
        queries.cpuBattleBrainRepository.save(CpuBattleBrain.startTurn(playerId = "cpu", battleUnitIds = listOf("rat-1")).pullEvents().second)
        eventBus.publish(CpuBattleBrainEvent.CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "rat-1"))
        // When
        eventBus.dispatch()
        // Then
        queries.cpuBattleBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldBeEmpty()
    }
}
