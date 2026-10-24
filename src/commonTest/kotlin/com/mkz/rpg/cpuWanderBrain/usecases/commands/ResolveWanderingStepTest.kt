package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.withLeaderApart
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.Test

class ResolveWanderingStepTest {
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.9))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveWanderingStep =
        ResolveWanderingStep(cpuWanderBrainRepository, WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus))

    // rat-1 stepped from its home (0,0) to (1,0), and rat-2 wanders next from its home (2,2). The party leader, knight-1,
    // stands on K.
    private fun givenRatStepped(battleUnitIds: List<String>) {
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield(withLeaderApart(". . .\nA . .\n. . B"), occupants = mapOf('A' to "rat-1", 'B' to "rat-2", 'K' to "knight-1")))
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        cpuWanderBrainRepository.save(
            cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2)))
                .startWhileExploring(exploringPlayerId = "human", battleUnitIds = battleUnitIds)
                .requestStep(battleUnitId = "rat-1", row = 1, column = 0),
        )
    }

    @Test
    fun `should let the next cpu battle unit wander once the awaited step has been walked`() {
        // Given
        givenRatStepped(battleUnitIds = listOf("rat-1", "rat-2"))
        // When
        resolveWanderingStep(battleUnitId = "rat-1", row = 1, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1))
    }

    @Test
    fun `should announce that the cpu battle units wandered once the last step has been walked`() {
        // Given
        givenRatStepped(battleUnitIds = listOf("rat-1"))
        // When
        resolveWanderingStep(battleUnitId = "rat-1", row = 1, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"), CpuBattleBrainEvent.RequestEngageEnemies(exploringPlayerId = "human"))
    }

    @Test
    fun `should ignore the tiles occupied by other battle units`() {
        // Given
        givenRatStepped(battleUnitIds = listOf("rat-1", "rat-2"))
        // When
        resolveWanderingStep(battleUnitId = "knight-1", row = 1, column = 0)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when nobody wanders`() {
        // When
        resolveWanderingStep(battleUnitId = "rat-1", row = 1, column = 0)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
