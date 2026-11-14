package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnPartySteppedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onPartyStepped = OnPartyStepped(RespawnDefeatedBattleUnits(cpuWanderBrainRepository, queries.isTileVacant, eventBus), eventBus)

    @Test
    fun `should count the party step for every defeated cpu battle unit when the party steps`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))).awaitRespawn("rat-1"))
        eventBus.publish(PartyEvent.PartyStepped(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        eventBus.dispatch()
        // Then
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .awaitedRespawns
            .shouldContainExactly(CpuWanderBrain.Dto.AwaitedRespawnDto(battleUnitId = "rat-1", partySteps = 1))
    }
}
