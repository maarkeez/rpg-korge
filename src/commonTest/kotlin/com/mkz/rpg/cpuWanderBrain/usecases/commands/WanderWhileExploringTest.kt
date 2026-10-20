package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WanderWhileExploringTest {
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
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
        val wanderWhileExploring =
            WanderWhileExploring(cpuWanderBrainRepository, WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus), eventBus)
        wanderWhileExploring(exploringPlayerId = "human")
    }

    @Test
    fun `should request the step of the first cpu battle unit that does not rest`() {
        // Given: rat-1 is at home, at (0,0), so it can step east or south.
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.9, 0.9)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-1", walkToRow = 1, walkToColumn = 0),
        )
        cpuWanderBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldContainExactly("rat-2")
    }

    @Test
    fun `should let the next cpu battle unit wander when the first one rests`() {
        // Given: rat-2 is at home, at (2,2), so it can step north or west.
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.0, 0.9)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1),
        )
    }

    @Test
    fun `should announce that the cpu battle units wandered when every one rests`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.0)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"),
        )
    }

    @Test
    fun `should let the cpu battle units that are no longer on the battlefield rest`() {
        // Given: rat-1 has been defeated.
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.9, 0.9, map = ". . .\n. . .\n. . B")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-2", walkToRow = 2, walkToColumn = 1),
        )
    }

    @Test
    fun `should make a cpu battle unit outside its territory return home`() {
        // Given: rat-1 is 4 tiles away from home, at (0,0), and rat-2 is not on the battlefield.
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.9, map = ". . . . A")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-1", walkToRow = 0, walkToColumn = 3),
        )
        cpuWanderBrainRepository.search()!!.isReturning("rat-1").shouldBeTrue()
    }

    @Test
    fun `should keep a returning cpu battle unit heading home without resting within its territory`() {
        // Given: rat-1 left its territory, and is now 2 tiles away from home, at (0,0).
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))).leaveTerritory("rat-1"))
        // When
        wanderWhileExploring(0.0, map = ". . A")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "rat-1", walkToRow = 0, walkToColumn = 1),
        )
        cpuWanderBrainRepository.search()!!.isReturning("rat-1").shouldBeTrue()
    }

    @Test
    fun `should let a returning cpu battle unit wander again once it is back home`() {
        // Given: rat-1 left its territory, and is now back home, at (0,0).
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))).leaveTerritory("rat-1"))
        // When
        wanderWhileExploring(0.0, map = "A . .")
        // Then: it rests, as any wandering battle unit at home may.
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10),
            CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"),
        )
        cpuWanderBrainRepository.search()!!.isReturning("rat-1").shouldBeFalse()
    }

    @Test
    fun `should request the recovery of every cpu battle unit with a home, whether it rests or steps`() {
        // Given
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))))
        // When
        wanderWhileExploring(0.0, 0.9)
        // Then
        eventBus.publishedEvents.first() shouldBe BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = WANDERING_RECOVERY_PERCENTAGE)
    }

    @Test
    fun `should not request any recovery when no cpu battle unit has a home`() {
        // Given
        cpuWanderBrainRepository.save(CpuWanderBrain.create())
        // When
        wanderWhileExploring(0.9)
        // Then
        eventBus.shouldHavePublishedEvents(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
    }

    @Test
    fun `should do nothing when nobody has claimed a home yet`() {
        // When
        wanderWhileExploring(0.9)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
