package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinAndLeaveBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestJoinAndLeaveBattleTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestJoinAndLeaveBattle =
        OnRequestJoinAndLeaveBattle(
            JoinAndLeaveBattle(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
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
