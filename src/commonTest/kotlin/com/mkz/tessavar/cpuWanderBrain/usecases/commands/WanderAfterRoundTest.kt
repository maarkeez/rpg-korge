package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.withLeaderApart
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import kotlin.test.Test

class WanderAfterRoundTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val eventBus = FakeEventBus()

    private fun wanderAfterRound(
        vararg rolls: Double,
        map: String = withLeaderApart("A . .\n. . .\n. . B"),
    ) {
        val queries = InMemoryQueries(random = FixedDoublesRandom(*rolls))
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('A' to "rat-1", 'B' to "rat-2", 'K' to "knight-1")))
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        val wanderAfterRound =
            WanderAfterRound(
                cpuWanderBrainRepository,
                WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus),
                queries.isInSight,
                eventBus,
            )
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
            BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1),
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
            CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
            CpuBattleBrainEvent.RequestJoinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1")),
        )
    }

    @Test
    fun `should only wander the cpu battle units out of the battle in sight of the party leader`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 1), "rat-2" to (0 to 11))))
        // When
        wanderAfterRound(0.9, 0.9, map = "K A . . . . . . . . . B . .")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-2"), percentage = 10),
            CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
            CpuBattleBrainEvent.RequestJoinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1")),
        )
    }

    @Test
    fun `should not request any recovery when every cpu battle unit is in the battle`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        // When
        wanderAfterRound(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
            CpuBattleBrainEvent.RequestJoinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1")),
        )
    }

    @Test
    fun `should announce that the cpu battle units wandered when no cpu battle unit has a home`() {
        // When
        wanderAfterRound(0.9)
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
            CpuBattleBrainEvent.RequestJoinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1")),
        )
    }
}
