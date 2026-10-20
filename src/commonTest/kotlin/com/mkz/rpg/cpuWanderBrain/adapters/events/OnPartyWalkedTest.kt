package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.RespawnDefeatedBattleUnits
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnPartyWalkedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onPartyWalked = OnPartyWalked(RespawnDefeatedBattleUnits(cpuWanderBrainRepository, queries.canBattlefieldTileBeOccupied, eventBus), eventBus)

    @Test
    fun `should count the party step for every defeated cpu battle unit when the party walks`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))).defeat("rat-1"))
        eventBus.publish(ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        eventBus.dispatch()
        // Then
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .respawns
            .shouldContainExactly(CpuWanderBrain.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 1))
    }
}
