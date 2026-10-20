package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnExplorationSecondStartedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.9))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onExplorationSecondStarted =
        OnExplorationSecondStarted(
            WanderWhileExploring(cpuWanderBrainRepository, WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus), eventBus),
            eventBus,
        )
    private val walkRequests = mutableListOf<BattleUnitEvent.RequestWalkBattleUnit>().also { requests -> eventBus.subscribe<BattleUnitEvent.RequestWalkBattleUnit> { requests += it } }

    @Test
    fun `should wander the cpu battle units when a second starts while exploring`() {
        // Given: the rat is at home, at (0,0), so it can step east or south.
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield("R .\n. .", occupants = mapOf('R' to "rat-1")))
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        eventBus.publish(ExplorationEvent.ExplorationSecondStarted(playerId = "human"))
        // When
        eventBus.dispatch()
        // Then
        walkRequests.shouldContainExactly(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-1", walkToRow = 1, walkToColumn = 0))
    }
}
