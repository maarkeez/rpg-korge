package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RestoreCpuWanderBrainTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val restoreCpuWanderBrain = RestoreCpuWanderBrain(cpuWanderBrainRepository)

    // rat-1 is returning home, and rat-2 was defeated 12 party steps ago.
    private val cpuWanderBrain: CpuWanderBrain =
        cpuWanderBrain()
            .leaveTerritory("rat-1")
            .defeat("rat-2")
            .let { brain -> (1..12).fold(brain) { walked, _ -> walked.walkPartyStep() } }
            .pullEvents()
            .second

    @Test
    fun `should rebuild the same state when it restores the snapshot it took`() {
        // Given
        cpuWanderBrainRepository.save(CpuWanderBrain.create())
        // When
        restoreCpuWanderBrain(cpuWanderBrain.toSnapshot())
        // Then
        cpuWanderBrainRepository.search() shouldBe cpuWanderBrain
    }

    @Test
    fun `should keep no CPU wander brain when the snapshot has none`() {
        // When
        restoreCpuWanderBrain(null)
        // Then
        cpuWanderBrainRepository.search().shouldBeNull()
    }
}
