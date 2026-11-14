package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ResolveWanderingStep
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.9))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onTileOccupied =
        OnTileOccupied(
            ResolveWanderingStep(cpuWanderBrainRepository, WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus)),
            eventBus,
        )
    private val wanderedEvents =
        mutableListOf<CpuWanderBrainEvent.CpuBattleUnitsWanderedWhileExploring>().also { wandered -> eventBus.subscribe<CpuWanderBrainEvent.CpuBattleUnitsWanderedWhileExploring> { wandered += it } }

    @Test
    fun `should resolve the awaited step when the wandering battle unit occupies its tile`() {
        // Given
        cpuWanderBrainRepository.save(
            cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0)))
                .startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1"))
                .requestStep(battleUnitId = "rat-1", row = 1, column = 0),
        )
        eventBus.publish(BattlefieldEvent.TileOccupied(row = 1, column = 0, occupantId = "rat-1"))
        // When
        eventBus.dispatch()
        // Then
        wanderedEvents.shouldContainExactly(CpuWanderBrainEvent.CpuBattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
    }
}
