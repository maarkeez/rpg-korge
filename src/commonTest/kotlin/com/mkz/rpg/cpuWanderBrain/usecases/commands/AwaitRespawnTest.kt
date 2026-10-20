package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class AwaitRespawnTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val awaitRespawn = AwaitRespawn(cpuWanderBrainRepository)

    @Test
    fun `should start counting the party steps of the defeated cpu battle unit`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        // When
        awaitRespawn(battleUnitId = "rat-1")
        // Then
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .respawns
            .shouldContainExactly(CpuWanderBrain.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 0))
    }

    @Test
    fun `should do nothing when no cpu battle unit has a home`() {
        // Given
        // When
        awaitRespawn(battleUnitId = "rat-1")
        // Then
        cpuWanderBrainRepository.search().shouldBeNull()
    }
}
