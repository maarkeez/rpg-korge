package com.mkz.rpg.cpuWanderBrain.domain

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CpuWanderBrainTest {
    @Test
    fun `Create should have no homes and nobody wandering`() {
        // When
        val cpuWanderBrain = CpuWanderBrain.create()
        // Then
        cpuWanderBrain.toDto() shouldBe CpuWanderBrain.Dto(homes = emptyList(), returningBattleUnitIds = emptySet(), pendingBattleUnitIds = emptyList(), isWandering = false, respawns = emptyList())
    }

    @Test
    fun `ClaimHome should remember the home of the battle unit`() {
        // When
        val cpuWanderBrain = CpuWanderBrain.create().claimHome(battleUnitId = "rat-1", row = 1, column = 2)
        // Then
        cpuWanderBrain.homeOf("rat-1") shouldBe CpuWanderBrain.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
        cpuWanderBrain.toDto().homes.shouldContainExactly(CpuWanderBrain.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2))
    }

    @Test
    fun `ClaimHome should keep the first home of the battle unit`() {
        // When
        val cpuWanderBrain = CpuWanderBrain.create().claimHome(battleUnitId = "rat-1", row = 1, column = 2).claimHome(battleUnitId = "rat-1", row = 3, column = 4)
        // Then
        cpuWanderBrain.homeOf("rat-1") shouldBe CpuWanderBrain.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
    }

    @Test
    fun `HomeOf should be null when the battle unit has no home`() {
        // When
        val home = cpuWanderBrain().homeOf("knight-1")
        // Then
        home.shouldBeNull()
    }

    @Test
    fun `LeaveTerritory should make the battle unit return home`() {
        // When
        val cpuWanderBrain = cpuWanderBrain().leaveTerritory("rat-1")
        // Then
        cpuWanderBrain.isReturning("rat-1").shouldBeTrue()
        cpuWanderBrain.isReturning("rat-2").shouldBeFalse()
        cpuWanderBrain.toDto().returningBattleUnitIds shouldBe setOf("rat-1")
    }

    @Test
    fun `ArriveHome should make the battle unit no longer return home`() {
        // When
        val cpuWanderBrain = cpuWanderBrain().leaveTerritory("rat-1").leaveTerritory("rat-2").arriveHome("rat-1")
        // Then
        cpuWanderBrain.isReturning("rat-1").shouldBeFalse()
        cpuWanderBrain.toDto().returningBattleUnitIds shouldBe setOf("rat-2")
    }

    @Test
    fun `ArriveHome should change nothing when the battle unit is not returning`() {
        // When
        val cpuWanderBrain = cpuWanderBrain().arriveHome("rat-1")
        // Then
        cpuWanderBrain shouldBe cpuWanderBrain()
    }

    @Test
    fun `StartWhileExploring should wander the battle units with a home, in order`() {
        // When
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-2", "knight-1", "rat-1"))
        // Then
        cpuWanderBrain.toDto().pendingBattleUnitIds.shouldContainExactly("rat-2", "rat-1")
        cpuWanderBrain.toDto().isWandering.shouldBeTrue()
        cpuWanderBrain.nextBattleUnitId() shouldBe "rat-2"
    }

    @Test
    fun `StartWhileExploring should announce at once that the battle units wandered when none has a home`() {
        // When
        val (events, cpuWanderBrain) = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("knight-1")).pullEvents()
        // Then
        events shouldBe setOf(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"), CpuBattleBrainEvent.RequestEngageEnemies(exploringPlayerId = "human"))
        cpuWanderBrain.toDto().isWandering.shouldBeFalse()
    }

    @Test
    fun `StartWhileExploring should abandon the awaited step of a wandering in progress`() {
        // Given
        val awaitingCpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val cpuWanderBrain = awaitingCpuWanderBrain.startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-2"))
        // Then
        cpuWanderBrain.nextBattleUnitId() shouldBe "rat-2"
    }

    @Test
    fun `Rest should let the next battle unit wander`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2"))
        // When
        val (events, restedCpuWanderBrain) = cpuWanderBrain.rest("rat-1").pullEvents()
        // Then
        restedCpuWanderBrain.nextBattleUnitId() shouldBe "rat-2"
        events.shouldBeEmpty()
    }

    @Test
    fun `Rest should announce that the battle units wandered when the last one rests`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1")).pullEvents().second
        // When
        val (events, restedCpuWanderBrain) = cpuWanderBrain.rest("rat-1").pullEvents()
        // Then
        events shouldBe setOf(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"), CpuBattleBrainEvent.RequestEngageEnemies(exploringPlayerId = "human"))
        restedCpuWanderBrain.toDto().isWandering.shouldBeFalse()
    }

    @Test
    fun `Rest should fail when it is not the next battle unit`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2"))
        // When / Then
        shouldThrow<CpuWanderBrainError.NotTheNextBattleUnit> { cpuWanderBrain.rest("rat-2") }
    }

    @Test
    fun `RequestStep should await the step before the next battle unit wanders`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2"))
        // When
        val steppingCpuWanderBrain = cpuWanderBrain.requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // Then
        steppingCpuWanderBrain.nextBattleUnitId().shouldBeNull()
        steppingCpuWanderBrain.toDto().pendingBattleUnitIds.shouldContainExactly("rat-2")
        steppingCpuWanderBrain.toDto().isWandering.shouldBeTrue()
    }

    @Test
    fun `RequestStep should fail while awaiting a step`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When / Then
        shouldThrow<CpuWanderBrainError.AlreadyAwaitingAStep> { cpuWanderBrain.requestStep(battleUnitId = "rat-2", row = 2, column = 1) }
    }

    @Test
    fun `ResolveStep should let the next battle unit wander when the awaited battle unit occupies its tile`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val resolvedCpuWanderBrain = cpuWanderBrain.resolveStep(battleUnitId = "rat-1", row = 0, column = 1)
        // Then
        resolvedCpuWanderBrain.nextBattleUnitId() shouldBe "rat-2"
    }

    @Test
    fun `ResolveStep should change nothing when another tile is occupied`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val resolvedCpuWanderBrain = cpuWanderBrain.resolveStep(battleUnitId = "rat-1", row = 1, column = 0)
        // Then
        resolvedCpuWanderBrain shouldBe cpuWanderBrain
    }

    @Test
    fun `ResolveStep should announce that the battle units wandered once when the last step is walked`() {
        // Given
        val cpuWanderBrain =
            cpuWanderBrain()
                .startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1"))
                .requestStep(battleUnitId = "rat-1", row = 0, column = 1)
                .pullEvents()
                .second
        // When
        val (events, resolvedCpuWanderBrain) = cpuWanderBrain.resolveStep(battleUnitId = "rat-1", row = 0, column = 1).pullEvents()
        // Then
        events shouldBe setOf(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"), CpuBattleBrainEvent.RequestEngageEnemies(exploringPlayerId = "human"))
        resolvedCpuWanderBrain.toDto().isWandering.shouldBeFalse()
        resolvedCpuWanderBrain
            .resolveStep(battleUnitId = "rat-1", row = 0, column = 1)
            .pullEvents()
            .first
            .shouldBeEmpty()
    }

    @Test
    fun `StartAfterRound should wander the battle units with a home out of the battle, in the order they were deployed`() {
        // When
        val cpuWanderBrain =
            cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2), "rat-3" to (4 to 4)))
                .startAfterRound(battleUnitIdsInBattle = setOf("knight-1", "rat-2"))
        // Then
        cpuWanderBrain.toDto().pendingBattleUnitIds.shouldContainExactly("rat-1", "rat-3")
    }

    @Test
    fun `StartAfterRound should announce at once that the battle units wandered, with the battle units in the battle, when every one is in the battle`() {
        // When
        val (events, _) = cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))).startAfterRound(battleUnitIdsInBattle = setOf("knight-1", "rat-1")).pullEvents()
        // Then
        events shouldBe
            setOf(
                CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")),
                CpuBattleBrainEvent.RequestUpdateEngagement(battleUnitIds = setOf("knight-1", "rat-1")),
            )
    }

    @Test
    fun `ResolveStep should announce that the battle units wandered after the round when the last step is walked`() {
        // Given
        val cpuWanderBrain =
            cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2)))
                .startAfterRound(battleUnitIdsInBattle = setOf("knight-1", "rat-2"))
                .requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val (events, _) = cpuWanderBrain.resolveStep(battleUnitId = "rat-1", row = 0, column = 1).pullEvents()
        // Then
        events shouldBe
            setOf(
                CpuWanderBrainEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-2")),
                CpuBattleBrainEvent.RequestUpdateEngagement(battleUnitIds = setOf("knight-1", "rat-2")),
            )
    }

    private fun CpuWanderBrain.walkPartySteps(steps: Int): CpuWanderBrain = (1..steps).fold(this) { cpuWanderBrain, _ -> cpuWanderBrain.walkPartyStep() }

    @Test
    fun `Defeat should start counting the party steps of a battle unit with a home`() {
        // When
        val cpuWanderBrain = cpuWanderBrain().defeat("rat-1")
        // Then
        cpuWanderBrain.toDto().respawns.shouldContainExactly(CpuWanderBrain.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 0))
    }

    @Test
    fun `Defeat should ignore a battle unit without a home`() {
        // When
        val cpuWanderBrain = cpuWanderBrain().defeat("knight-1")
        // Then
        cpuWanderBrain.toDto().respawns.shouldBeEmpty()
    }

    @Test
    fun `WalkPartyStep should count a step for every defeated battle unit`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().defeat("rat-1").walkPartyStep().defeat("rat-2")
        // When
        val walkedCpuWanderBrain = cpuWanderBrain.walkPartyStep()
        // Then
        walkedCpuWanderBrain.toDto().respawns.shouldContainExactly(
            CpuWanderBrain.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 2),
            CpuWanderBrain.Dto.RespawnDto(battleUnitId = "rat-2", partySteps = 1),
        )
    }

    @Test
    fun `DueRespawns should be the battle units that counted the respawn steps, with their home, in their deployment order`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().defeat("rat-2").defeat("rat-1")
        // When
        val dueRespawns = cpuWanderBrain.walkPartySteps(RESPAWN_STEPS).dueRespawns()
        // Then
        dueRespawns.shouldContainExactly(
            CpuWanderBrain.Dto.HomeDto(battleUnitId = "rat-1", row = 0, column = 0),
            CpuWanderBrain.Dto.HomeDto(battleUnitId = "rat-2", row = 2, column = 2),
        )
    }

    @Test
    fun `DueRespawns should not have the battle units that counted fewer steps`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().defeat("rat-1").walkPartySteps(RESPAWN_STEPS - 1)
        // When
        val dueRespawns = cpuWanderBrain.dueRespawns()
        // Then
        dueRespawns.shouldBeEmpty()
    }

    @Test
    fun `Respawn should stop counting the party steps of the battle unit, which no longer returns`() {
        // Given
        val cpuWanderBrain = cpuWanderBrain().leaveTerritory("rat-1").defeat("rat-1").walkPartySteps(RESPAWN_STEPS)
        // When
        val respawnedCpuWanderBrain = cpuWanderBrain.respawn("rat-1")
        // Then
        respawnedCpuWanderBrain.toDto().respawns.shouldBeEmpty()
        respawnedCpuWanderBrain.isReturning("rat-1").shouldBeFalse()
    }
}
