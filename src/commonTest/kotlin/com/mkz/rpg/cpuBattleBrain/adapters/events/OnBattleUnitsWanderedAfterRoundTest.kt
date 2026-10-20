package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitsWanderedAfterRoundTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onBattleUnitsWanderedAfterRound =
        OnBattleUnitsWanderedAfterRound(
            UpdateEngagement(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
            eventBus,
        )
    private val updates = mutableListOf<CpuBattleBrainEvent.EngagementUpdated>().also { updated -> eventBus.subscribe<CpuBattleBrainEvent.EngagementUpdated> { updated += it } }

    @Test
    fun `should update the engagement when the cpu battle units out of the battle have wandered after a round`() {
        // Given
        eventBus.publish(CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = emptySet()))
        // When
        eventBus.dispatch()
        // Then
        updates.shouldContainExactly(CpuBattleBrainEvent.EngagementUpdated(joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()))
    }
}
