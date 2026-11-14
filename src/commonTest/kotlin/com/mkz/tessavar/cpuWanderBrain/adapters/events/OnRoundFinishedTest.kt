package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.WanderAfterRound
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRoundFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.0))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onRoundFinished =
        OnRoundFinished(
            WanderAfterRound(
                cpuWanderBrainRepository,
                WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus),
                queries.isInSight,
                eventBus,
            ),
            eventBus,
        )
    private val wanderedEvents =
        mutableListOf<CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound>().also { wandered -> eventBus.subscribe<CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound> { wandered += it } }

    @Test
    fun `should wander the cpu battle units out of the battle when a round finishes`() {
        // Given
        eventBus.publish(BattleEvent.RoundFinished(round = 1, battleUnitIds = setOf("knight-1", "rat-1")))
        // When
        eventBus.dispatch()
        // Then
        wanderedEvents.shouldContainExactly(CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }
}
