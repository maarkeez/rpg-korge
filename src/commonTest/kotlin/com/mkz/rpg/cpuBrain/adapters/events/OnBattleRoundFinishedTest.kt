package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleRoundFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onBattleRoundFinished =
        OnBattleRoundFinished(
            UpdateEngagement(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
            eventBus,
        )
    private val updates = mutableListOf<CpuBrainEvent.EngagementUpdated>().also { updated -> eventBus.subscribe<CpuBrainEvent.EngagementUpdated> { updated += it } }

    @Test
    fun `should update the engagement when a battle round finishes`() {
        // Given
        eventBus.publish(BattleEvent.BattleRoundFinished(round = 1, battleUnitIds = emptySet()))
        // When
        eventBus.dispatch()
        // Then
        updates.shouldContainExactly(CpuBrainEvent.EngagementUpdated(joiningBattleUnitIds = emptyList()))
    }
}
