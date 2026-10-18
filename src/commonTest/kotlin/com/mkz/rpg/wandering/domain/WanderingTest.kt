package com.mkz.rpg.wandering.domain

import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WanderingTest {
    @Test
    fun `Create should have no homes and nobody wandering`() {
        // When
        val wandering = Wandering.create()
        // Then
        wandering.toDto() shouldBe Wandering.Dto(homes = emptyList(), returningBattleUnitIds = emptySet(), pendingBattleUnitIds = emptyList(), isWandering = false, respawns = emptyList())
    }

    @Test
    fun `ClaimHome should remember the home of the battle unit`() {
        // When
        val wandering = Wandering.create().claimHome(battleUnitId = "rat-1", row = 1, column = 2)
        // Then
        wandering.homeOf("rat-1") shouldBe Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
        wandering.toDto().homes.shouldContainExactly(Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2))
    }

    @Test
    fun `ClaimHome should keep the first home of the battle unit`() {
        // When
        val wandering = Wandering.create().claimHome(battleUnitId = "rat-1", row = 1, column = 2).claimHome(battleUnitId = "rat-1", row = 3, column = 4)
        // Then
        wandering.homeOf("rat-1") shouldBe Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
    }

    @Test
    fun `HomeOf should be null when the battle unit has no home`() {
        // When
        val home = wandering().homeOf("knight-1")
        // Then
        home.shouldBeNull()
    }

    @Test
    fun `LeaveTerritory should make the battle unit return home`() {
        // When
        val wandering = wandering().leaveTerritory("rat-1")
        // Then
        wandering.isReturning("rat-1").shouldBeTrue()
        wandering.isReturning("rat-2").shouldBeFalse()
        wandering.toDto().returningBattleUnitIds shouldBe setOf("rat-1")
    }

    @Test
    fun `ArriveHome should make the battle unit no longer return home`() {
        // When
        val wandering = wandering().leaveTerritory("rat-1").leaveTerritory("rat-2").arriveHome("rat-1")
        // Then
        wandering.isReturning("rat-1").shouldBeFalse()
        wandering.toDto().returningBattleUnitIds shouldBe setOf("rat-2")
    }

    @Test
    fun `ArriveHome should change nothing when the battle unit is not returning`() {
        // When
        val wandering = wandering().arriveHome("rat-1")
        // Then
        wandering shouldBe wandering()
    }

    @Test
    fun `StartWhileExploring should wander the battle units with a home, in order`() {
        // When
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-2", "knight-1", "rat-1"))
        // Then
        wandering.toDto().pendingBattleUnitIds.shouldContainExactly("rat-2", "rat-1")
        wandering.toDto().isWandering.shouldBeTrue()
        wandering.nextBattleUnitId() shouldBe "rat-2"
    }

    @Test
    fun `StartWhileExploring should announce at once that the battle units wandered when none has a home`() {
        // When
        val (events, wandering) = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("knight-1")).pullEvents()
        // Then
        events shouldBe setOf(WanderingEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
        wandering.toDto().isWandering.shouldBeFalse()
    }

    @Test
    fun `StartWhileExploring should abandon the awaited step of a wandering in progress`() {
        // Given
        val awaitingWandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val wandering = awaitingWandering.startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-2"))
        // Then
        wandering.nextBattleUnitId() shouldBe "rat-2"
    }

    @Test
    fun `Rest should let the next battle unit wander`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2"))
        // When
        val (events, restedWandering) = wandering.rest("rat-1").pullEvents()
        // Then
        restedWandering.nextBattleUnitId() shouldBe "rat-2"
        events.shouldBeEmpty()
    }

    @Test
    fun `Rest should announce that the battle units wandered when the last one rests`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1")).pullEvents().second
        // When
        val (events, restedWandering) = wandering.rest("rat-1").pullEvents()
        // Then
        events shouldBe setOf(WanderingEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
        restedWandering.toDto().isWandering.shouldBeFalse()
    }

    @Test
    fun `Rest should fail when it is not the next battle unit`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2"))
        // When / Then
        shouldThrow<WanderingError.NotTheNextBattleUnit> { wandering.rest("rat-2") }
    }

    @Test
    fun `RequestStep should await the step before the next battle unit wanders`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2"))
        // When
        val steppingWandering = wandering.requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // Then
        steppingWandering.nextBattleUnitId().shouldBeNull()
        steppingWandering.toDto().pendingBattleUnitIds.shouldContainExactly("rat-2")
        steppingWandering.toDto().isWandering.shouldBeTrue()
    }

    @Test
    fun `RequestStep should fail while awaiting a step`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When / Then
        shouldThrow<WanderingError.AlreadyAwaitingAStep> { wandering.requestStep(battleUnitId = "rat-2", row = 2, column = 1) }
    }

    @Test
    fun `ResolveStep should let the next battle unit wander when the awaited battle unit occupies its tile`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val resolvedWandering = wandering.resolveStep(battleUnitId = "rat-1", row = 0, column = 1)
        // Then
        resolvedWandering.nextBattleUnitId() shouldBe "rat-2"
    }

    @Test
    fun `ResolveStep should change nothing when another tile is occupied`() {
        // Given
        val wandering = wandering().startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1", "rat-2")).requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val resolvedWandering = wandering.resolveStep(battleUnitId = "rat-1", row = 1, column = 0)
        // Then
        resolvedWandering shouldBe wandering
    }

    @Test
    fun `ResolveStep should announce that the battle units wandered once when the last step is walked`() {
        // Given
        val wandering =
            wandering()
                .startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1"))
                .requestStep(battleUnitId = "rat-1", row = 0, column = 1)
                .pullEvents()
                .second
        // When
        val (events, resolvedWandering) = wandering.resolveStep(battleUnitId = "rat-1", row = 0, column = 1).pullEvents()
        // Then
        events shouldBe setOf(WanderingEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
        resolvedWandering.toDto().isWandering.shouldBeFalse()
        resolvedWandering
            .resolveStep(battleUnitId = "rat-1", row = 0, column = 1)
            .pullEvents()
            .first
            .shouldBeEmpty()
    }

    @Test
    fun `StartAfterRound should wander the battle units with a home out of the battle, in the order they were deployed`() {
        // When
        val wandering =
            wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2), "rat-3" to (4 to 4)))
                .startAfterRound(battleUnitIdsInBattle = setOf("knight-1", "rat-2"))
        // Then
        wandering.toDto().pendingBattleUnitIds.shouldContainExactly("rat-1", "rat-3")
    }

    @Test
    fun `StartAfterRound should announce at once that the battle units wandered, with the battle units in the battle, when every one is in the battle`() {
        // When
        val (events, _) = wandering(homes = mapOf("rat-1" to (0 to 0))).startAfterRound(battleUnitIdsInBattle = setOf("knight-1", "rat-1")).pullEvents()
        // Then
        events shouldBe setOf(WanderingEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }

    @Test
    fun `ResolveStep should announce that the battle units wandered after the round when the last step is walked`() {
        // Given
        val wandering =
            wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2)))
                .startAfterRound(battleUnitIdsInBattle = setOf("knight-1", "rat-2"))
                .requestStep(battleUnitId = "rat-1", row = 0, column = 1)
        // When
        val (events, _) = wandering.resolveStep(battleUnitId = "rat-1", row = 0, column = 1).pullEvents()
        // Then
        events shouldBe setOf(WanderingEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-2")))
    }

    private fun Wandering.walkPartySteps(steps: Int): Wandering = (1..steps).fold(this) { wandering, _ -> wandering.walkPartyStep() }

    @Test
    fun `Defeat should start counting the party steps of a battle unit with a home`() {
        // When
        val wandering = wandering().defeat("rat-1")
        // Then
        wandering.toDto().respawns.shouldContainExactly(Wandering.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 0))
    }

    @Test
    fun `Defeat should ignore a battle unit without a home`() {
        // When
        val wandering = wandering().defeat("knight-1")
        // Then
        wandering.toDto().respawns.shouldBeEmpty()
    }

    @Test
    fun `WalkPartyStep should count a step for every defeated battle unit`() {
        // Given
        val wandering = wandering().defeat("rat-1").walkPartyStep().defeat("rat-2")
        // When
        val walkedWandering = wandering.walkPartyStep()
        // Then
        walkedWandering.toDto().respawns.shouldContainExactly(
            Wandering.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 2),
            Wandering.Dto.RespawnDto(battleUnitId = "rat-2", partySteps = 1),
        )
    }

    @Test
    fun `DueRespawns should be the battle units that counted the respawn steps, with their home, in their deployment order`() {
        // Given
        val wandering = wandering().defeat("rat-2").defeat("rat-1")
        // When
        val dueRespawns = wandering.walkPartySteps(RESPAWN_STEPS).dueRespawns()
        // Then
        dueRespawns.shouldContainExactly(
            Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 0, column = 0),
            Wandering.Dto.HomeDto(battleUnitId = "rat-2", row = 2, column = 2),
        )
    }

    @Test
    fun `DueRespawns should not have the battle units that counted fewer steps`() {
        // Given
        val wandering = wandering().defeat("rat-1").walkPartySteps(RESPAWN_STEPS - 1)
        // When
        val dueRespawns = wandering.dueRespawns()
        // Then
        dueRespawns.shouldBeEmpty()
    }

    @Test
    fun `Respawn should stop counting the party steps of the battle unit, which no longer returns`() {
        // Given
        val wandering = wandering().leaveTerritory("rat-1").defeat("rat-1").walkPartySteps(RESPAWN_STEPS)
        // When
        val respawnedWandering = wandering.respawn("rat-1")
        // Then
        respawnedWandering.toDto().respawns.shouldBeEmpty()
        respawnedWandering.isReturning("rat-1").shouldBeFalse()
    }
}
