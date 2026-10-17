package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingEvent
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import com.mkz.rpg.wandering.usecases.services.WanderingSteps
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class WanderWhileExploringTest {
    private val wanderingRepository = InMemoryWanderingRepository()
    private val eventBus = FakeEventBus()

    // A rest roll below 0.5 rests. Otherwise, the next roll picks the step: each step from home is as likely.
    private fun wanderWhileExploring(
        vararg rolls: Double,
        map: String =
            """
            A . .
            . . .
            . . B
            """,
    ) {
        val queries = InMemoryQueries(random = FixedDoublesRandom(*rolls))
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('A' to "rat-1", 'B' to "rat-2")))
        val wanderWhileExploring = WanderWhileExploring(wanderingRepository, WanderingSteps(wanderingRepository, queries.whereShouldWander, eventBus))
        wanderWhileExploring(exploringPlayerId = "human")
    }

    @Test
    fun `should request the step of the first cpu battle unit that does not rest`() {
        // Given: rat-1 is at home, at (0,0), so it can step east or south.
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.9, 0.9)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-1", walkToRow = 1, walkToColumn = 0))
        wanderingRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldContainExactly("rat-2")
    }

    @Test
    fun `should let the next cpu battle unit wander when the first one rests`() {
        // Given: rat-2 is at home, at (2,2), so it can step north or west.
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.0, 0.9)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1))
    }

    @Test
    fun `should announce that the cpu battle units wandered when every one rests`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(WanderingEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
    }

    @Test
    fun `should let the cpu battle units that are no longer on the battlefield rest`() {
        // Given: rat-1 has been defeated.
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.9, 0.9, map = ". . .\n. . .\n. . B")
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1))
    }

    @Test
    fun `should do nothing when no cpu battle unit has a home`() {
        // When
        wanderWhileExploring(0.9)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
