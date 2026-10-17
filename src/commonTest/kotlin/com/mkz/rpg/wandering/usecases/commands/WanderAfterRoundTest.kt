package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingEvent
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import com.mkz.rpg.wandering.usecases.services.WanderingSteps
import kotlin.test.Test

class WanderAfterRoundTest {
    private val wanderingRepository = InMemoryWanderingRepository()
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
        val wanderAfterRound = WanderAfterRound(wanderingRepository, WanderingSteps(wanderingRepository, queries.whereShouldWander, eventBus), eventBus)
        wanderAfterRound(battleUnitIds = setOf("knight-1", "rat-1"))
    }

    @Test
    fun `should request the step of the cpu battle units out of the battle when a round finishes`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
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
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderAfterRound(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-2"), percentage = 10),
            WanderingEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
        )
    }

    @Test
    fun `should not request any recovery when every cpu battle unit is in the battle`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))))
        // When
        wanderAfterRound(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(WanderingEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }

    @Test
    fun `should announce that the cpu battle units wandered when no cpu battle unit has a home`() {
        // When
        wanderAfterRound(0.9)
        // Then
        eventBus.shouldHavePublishedEvents(WanderingEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }
}
