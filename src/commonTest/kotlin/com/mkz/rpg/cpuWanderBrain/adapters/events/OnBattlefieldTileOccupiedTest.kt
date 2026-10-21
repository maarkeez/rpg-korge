package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ResolveWanderingStep
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattlefieldTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.9))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onBattlefieldTileOccupied =
        OnBattlefieldTileOccupied(
            ResolveWanderingStep(cpuWanderBrainRepository, WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus)),
            eventBus,
        )
    private val wanderedEvents =
        mutableListOf<CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring>().also { wandered -> eventBus.subscribe<CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring> { wandered += it } }

    @Test
    fun `should resolve the awaited step when the wandering battle unit occupies its tile`() {
        // Given
        cpuWanderBrainRepository.save(
            cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0)))
                .startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1"))
                .requestStep(battleUnitId = "rat-1", row = 1, column = 0),
        )
        eventBus.publish(BattlefieldEvent.BattlefieldTileOccupied(row = 1, column = 0, occupantId = "rat-1"))
        // When
        eventBus.dispatch()
        // Then
        wanderedEvents.shouldContainExactly(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
    }
}
