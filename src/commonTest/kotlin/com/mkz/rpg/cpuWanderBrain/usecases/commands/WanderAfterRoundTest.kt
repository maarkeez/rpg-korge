package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.Test

class WanderAfterRoundTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val eventBus = FakeEventBus()

    // rat-1 battles from (0,0), and rat-2 is out of the battle, at home at (2,2), so it can step north or west. A rest
    // roll below 0.5 rests. Otherwise, the next roll picks the step.
    private fun wanderAfterRound(vararg rolls: Double) {
        val queries = InMemoryQueries(random = FixedDoublesRandom(*rolls))
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield("A . .\n. . .\n. . B", occupants = mapOf('A' to "rat-1", 'B' to "rat-2")))
        val wanderAfterRound =
            WanderAfterRound(cpuWanderBrainRepository, WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus), eventBus)
        wanderAfterRound(battleUnitIds = setOf("knight-1", "rat-1"))
    }

    @Test
    fun `should request the step of the cpu battle units out of the battle when a round finishes`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderAfterRound(0.9, 0.9)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-2"), percentage = 10),
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1),
        )
    }

    @Test
    fun `should announce that the cpu battle units wandered, with the battle units in the battle, when every one out of it rests`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderAfterRound(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-2"), percentage = 10),
            CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
        )
    }

    @Test
    fun `should not request any recovery when every cpu battle unit is in the battle`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        // When
        wanderAfterRound(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }

    @Test
    fun `should announce that the cpu battle units wandered when no cpu battle unit has a home`() {
        // When
        wanderAfterRound(0.9)
        // Then
        eventBus.shouldHavePublishedEvents(CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }
}
