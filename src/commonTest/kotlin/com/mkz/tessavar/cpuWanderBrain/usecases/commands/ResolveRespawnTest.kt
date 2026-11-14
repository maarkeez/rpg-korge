package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class ResolveRespawnTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val resolveRespawn = ResolveRespawn(cpuWanderBrainRepository)

    @Test
    fun `should stop counting the party steps of the respawned battle unit`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))).awaitRespawn("rat-1").walkPartyStep())
        // When
        resolveRespawn(battleUnitId = "rat-1")
        // Then
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .awaitedRespawns
            .shouldBeEmpty()
    }
}
