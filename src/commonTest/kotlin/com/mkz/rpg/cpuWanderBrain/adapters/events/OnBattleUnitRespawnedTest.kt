package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ResolveRespawn
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class OnBattleUnitRespawnedTest {
    private val eventBus = InMemoryEventBus()
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onBattleUnitRespawned = OnBattleUnitRespawned(ResolveRespawn(cpuWanderBrainRepository), eventBus)

    @Test
    fun `should stop counting the party steps when a cpu battle unit respawns`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))).defeat("rat-1"))
        eventBus.publish(BattleUnitEvent.BattleUnitRespawned(playerId = "cpu", battleUnitId = "rat-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .respawns
            .shouldBeEmpty()
    }
}
