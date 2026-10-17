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
        wandering.toDto() shouldBe Wandering.Dto(homes = emptyList(), pendingBattleUnitIds = emptyList(), isWandering = false)
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
}
