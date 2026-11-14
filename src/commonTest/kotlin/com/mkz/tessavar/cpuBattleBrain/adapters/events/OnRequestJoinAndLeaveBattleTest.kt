package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.JoinAndLeaveBattle
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestJoinAndLeaveBattleTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestJoinAndLeaveBattle =
        OnRequestJoinAndLeaveBattle(
            JoinAndLeaveBattle(queries.searchHumanPlayer, queries.searchOpponent, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
            eventBus,
        )
    private val updates = mutableListOf<CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft>().also { updated -> eventBus.subscribe<CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft> { updated += it } }

    @Test
    fun `should have the cpu battle units join and leave the battle when requested after the cpu battle units out of the battle have wandered`() {
        // Given
        eventBus.publish(CpuBattleBrainEvent.RequestJoinAndLeaveBattle(battleUnitIds = emptySet()))
        // When
        eventBus.dispatch()
        // Then
        updates.shouldContainExactly(CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()))
    }
}
