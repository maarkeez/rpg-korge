package com.mkz.rpg.cpuWanderBrain.adapters.save

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CpuWanderBrainSnapshottableTest {
    // rat-1 is returning home, and rat-2 was defeated 12 party steps ago.
    private val cpuWanderBrain: CpuWanderBrain =
        cpuWanderBrain()
            .leaveTerritory("rat-1")
            .defeat("rat-2")
            .let { brain -> (1..12).fold(brain) { walked, _ -> walked.walkPartyStep() } }
            .pullEvents()
            .second

    private fun snapshot(): String = CpuWanderBrainSnapshottable(InMemoryCpuWanderBrainRepository().apply { save(cpuWanderBrain) }).snapshot()

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val restoredRepository = InMemoryCpuWanderBrainRepository().apply { save(CpuWanderBrain.create()) }
        // When
        CpuWanderBrainSnapshottable(restoredRepository).restore(snapshot())
        // Then
        restoredRepository.search() shouldBe cpuWanderBrain
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        val restoredRepository = InMemoryCpuWanderBrainRepository()
        // When
        CpuWanderBrainSnapshottable(restoredRepository).restore(snapshot())
        // Then
        restoredRepository
            .search()!!
            .pullEvents()
            .first
            .shouldBeEmpty()
    }
}
