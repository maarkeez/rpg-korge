package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class OnCpuBattleUnitManagedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val manageCpuBattleUnit =
        ManageCpuBattleUnit(queries.cpuBattleBrainRepository, queries.searchBattleUnitsByPlayerId, queries.whatShouldCast, queries.whereShouldMove, eventBus)
    private val listener = OnCpuBattleUnitManaged(manageCpuBattleUnit, eventBus)

    @Test
    fun `should manage the next cpu battle unit when a cpu battle unit is managed is dispatched`() {
        // Given
        // The rat has been defeated, so it has nothing left to do this turn.
        queries.cpuBattleBrainRepository.save(CpuBattleBrain.startTurn(playerId = "cpu", battleUnitIds = listOf("rat-1")).pullEvents().second)
        eventBus.publish(CpuBattleBrainEvent.CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "skull-1"))
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
