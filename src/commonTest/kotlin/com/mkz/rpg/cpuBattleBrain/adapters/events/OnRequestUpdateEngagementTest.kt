package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestUpdateEngagementTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestUpdateEngagement =
        OnRequestUpdateEngagement(
            UpdateEngagement(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
            eventBus,
        )
    private val updates = mutableListOf<CpuBattleBrainEvent.EngagementUpdated>().also { updated -> eventBus.subscribe<CpuBattleBrainEvent.EngagementUpdated> { updated += it } }

    @Test
    fun `should update the engagement when requested after the cpu battle units out of the battle have wandered`() {
        // Given
        eventBus.publish(CpuBattleBrainEvent.RequestUpdateEngagement(battleUnitIds = emptySet()))
        // When
        eventBus.dispatch()
        // Then
        updates.shouldContainExactly(CpuBattleBrainEvent.EngagementUpdated(playerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()))
    }
}
