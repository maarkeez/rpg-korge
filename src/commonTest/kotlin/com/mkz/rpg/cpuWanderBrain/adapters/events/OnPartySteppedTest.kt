package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
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
