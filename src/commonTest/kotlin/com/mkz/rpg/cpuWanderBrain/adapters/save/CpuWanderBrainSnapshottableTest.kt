package com.mkz.rpg.cpuWanderBrain.adapters.save

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.commands.RestoreCpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.queries.SearchCpuWanderBrainSnapshot
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

    private fun snapshottable(repository: CpuWanderBrainRepository) = CpuWanderBrainSnapshottable(SearchCpuWanderBrainSnapshot(repository), RestoreCpuWanderBrain(repository))

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryCpuWanderBrainRepository().apply { save(cpuWanderBrain) }).snapshot()
        val restoredRepository = InMemoryCpuWanderBrainRepository()
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.search() shouldBe cpuWanderBrain
    }

    @Test
    fun `Key should be cpuWanderBrain`() {
        // When
        val result = snapshottable(InMemoryCpuWanderBrainRepository()).key
        // Then
        result shouldBe "cpuWanderBrain"
    }
}
