package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderAfterRound
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleRoundFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.0))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onBattleRoundFinished =
        OnBattleRoundFinished(
            WanderAfterRound(
                cpuWanderBrainRepository,
                WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus),
                queries.isInSight,
                eventBus,
            ),
            eventBus,
        )
    private val wanderedEvents =
        mutableListOf<CpuWanderBrainEvent.BattleUnitsWanderedAfterRound>().also { wandered -> eventBus.subscribe<CpuWanderBrainEvent.BattleUnitsWanderedAfterRound> { wandered += it } }

    @Test
    fun `should wander the cpu battle units out of the battle when a battle round finishes`() {
        // Given
        eventBus.publish(BattleEvent.BattleRoundFinished(round = 1, battleUnitIds = setOf("knight-1", "rat-1")))
        // When
        eventBus.dispatch()
        // Then
        wanderedEvents.shouldContainExactly(CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }
}
