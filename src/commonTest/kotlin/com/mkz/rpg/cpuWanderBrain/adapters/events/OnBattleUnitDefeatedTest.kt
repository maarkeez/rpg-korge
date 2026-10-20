package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.AwaitRespawn
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onBattleUnitDefeated = OnBattleUnitDefeated(AwaitRespawn(cpuWanderBrainRepository), eventBus)

    @Test
    fun `should start counting the party steps when a cpu battle unit is defeated`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "cpu", battleUnitId = "rat-1", defeatedAtRow = 1, defeatedAtColumn = 1))
        // When
        eventBus.dispatch()
        // Then
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .respawns
            .shouldContainExactly(CpuWanderBrain.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 0))
    }
}
