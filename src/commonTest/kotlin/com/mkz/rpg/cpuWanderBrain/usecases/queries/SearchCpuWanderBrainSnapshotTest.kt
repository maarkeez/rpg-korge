package com.mkz.rpg.cpuWanderBrain.usecases.queries

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchCpuWanderBrainSnapshotTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val searchCpuWanderBrainSnapshot = SearchCpuWanderBrainSnapshot(cpuWanderBrainRepository)

    @Test
    fun `should return the battle units returning home and the party steps counted until each respawns`() {
        // Given: rat-1 is returning home, and rat-2 was defeated 12 party steps ago.
        cpuWanderBrainRepository.save(
            cpuWanderBrain()
                .leaveTerritory("rat-1")
                .defeat("rat-2")
                .let { brain -> (1..12).fold(brain) { walked, _ -> walked.walkPartyStep() } }
                .pullEvents()
                .second,
        )
        // When
        val result = searchCpuWanderBrainSnapshot()!!
        // Then
        result.returningBattleUnitIds shouldBe setOf("rat-1")
        result.respawns.map { respawn -> respawn.battleUnitId to respawn.partySteps } shouldBe listOf("rat-2" to 12)
    }

    @Test
    fun `should return nothing when there is no CPU wander brain`() {
        // When
        val result = searchCpuWanderBrainSnapshot()
        // Then
        result.shouldBeNull()
    }
}
