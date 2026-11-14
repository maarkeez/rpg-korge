package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.MakeNextDecision
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class OnCpuBattleUnitDecisionResolvedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val makeNextDecision =
        MakeNextDecision(queries.cpuBattleBrainRepository, queries.searchBattleUnitsByPlayerId, queries.whatShouldCast, queries.whereShouldWalk, eventBus)
    private val listener = OnCpuBattleUnitDecisionResolved(makeNextDecision, eventBus)

    @Test
    fun `should make the next decision when the cpu battle unit decision is resolved`() {
        // Given
        queries.cpuBattleBrainRepository.save(CpuBattleBrain.startPlayerTurn(playerId = "cpu", battleUnitIds = listOf("rat-1")).pullEvents().second)
        eventBus.publish(CpuBattleBrainEvent.CpuBattleUnitDecisionResolved(playerId = "cpu", battleUnitId = "rat-1"))
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
