package com.mkz.rpg.cpuWanderBrain.adapters.storage

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryCpuWanderBrainRepositoryTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()

    @Test
    fun `should find nothing when no cpu wander brain has been saved`() {
        // When
        val cpuWanderBrain = cpuWanderBrainRepository.search()
        // Then
        cpuWanderBrain.shouldBeNull()
    }

    @Test
    fun `should find the last saved cpu wander brain`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-2" to (1 to 1))))
        // When
        val cpuWanderBrain = cpuWanderBrainRepository.search()
        // Then
        cpuWanderBrain shouldBe cpuWanderBrain(homes = mapOf("rat-2" to (1 to 1)))
    }
}
