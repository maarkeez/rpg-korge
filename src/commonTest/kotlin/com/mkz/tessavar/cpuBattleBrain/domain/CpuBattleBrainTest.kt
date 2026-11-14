package com.mkz.tessavar.cpuBattleBrain.domain

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainError.AlreadyAwaitingADecision
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainError.NoBattleUnitsToDecideFor
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainError.NotTheNextBattleUnit
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDecisionResolved
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDone
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnFinished
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnStarted
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CpuBattleBrainTest {
    @Test
    fun `StartPlayerTurn should leave the battle units pending in order when the player turn starts`() {
        // Given
        val battleUnitIds = listOf("unit-1", "unit-2")
        // When
        val (events, cpuBattleBrain) = CpuBattleBrain.startPlayerTurn(playerId = "cpu", battleUnitIds = battleUnitIds).pullEvents()
        // Then
        cpuBattleBrain.toDto() shouldBe CpuBattleBrain.Dto(playerId = "cpu", pendingBattleUnitIds = battleUnitIds, isAwaitingDecision = false, decisionsTaken = 0)
        cpuBattleBrain.nextBattleUnitId() shouldBe "unit-1"
        events.shouldContainExactly(CpuPlayerTurnStarted(playerId = "cpu"))
    }

    @Test
    fun `StartPlayerTurn should fail when there are no battle units to decide for`() {
        // Given
        val battleUnitIds = emptyList<String>()
        // When
        val start = { CpuBattleBrain.startPlayerTurn(playerId = "cpu", battleUnitIds = battleUnitIds) }
        // Then
        shouldThrow<NoBattleUnitsToDecideFor> { start() }
    }

    @Test
    fun `RequestWalk should await the walk and count the decision when the next battle unit walks`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.requestWalk(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeTrue()
        updatedCpuBattleBrain.toDto().decisionsTaken shouldBe 1
        events.shouldBeEmpty()
    }

    @Test
    fun `RequestWalk should fail when the battle unit is not the next one to decide for`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val move = { cpuBattleBrain.requestWalk(battleUnitId = "unit-2", row = 1, column = 2) }
        // Then
        shouldThrow<NotTheNextBattleUnit> { move() }
    }

    @Test
    fun `RequestWalk should fail when the previous decision is still awaited`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val move = { cpuBattleBrain.requestWalk(battleUnitId = "unit-1", row = 1, column = 2) }
        // Then
        shouldThrow<AlreadyAwaitingADecision> { move() }
    }

    @Test
    fun `RequestCast should await the cast and count the decision when the next battle unit casts`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val updatedCpuBattleBrain = cpuBattleBrain.requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeTrue()
        updatedCpuBattleBrain.toDto().decisionsTaken shouldBe 1
    }

    @Test
    fun `ResolveWalk should continue when the awaited walk reaches the requested tile`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestWalk(battleUnitId = "unit-1", row = 1, column = 2)
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveWalk(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeFalse()
        events.shouldContainExactly(CpuBattleUnitDecisionResolved(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `ResolveWalk should keep waiting when another tile is occupied`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestWalk(battleUnitId = "unit-1", row = 1, column = 2)
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveWalk(battleUnitId = "unit-1", row = 5, column = 5).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `ResolveWalk should keep waiting when a cast is awaited`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "teleport")
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveWalk(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `ResolveCast should continue when the awaited cast is resolved`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveCast(battleUnitId = "unit-1", abilityId = "sword").pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeFalse()
        events.shouldContainExactly(CpuBattleUnitDecisionResolved(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `ResolveCast should keep waiting when another battle unit cast is resolved`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveCast(battleUnitId = "unit-2", abilityId = "sword").pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `MarkDone should continue with the next battle unit when the battle unit is done`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.markDone("unit-1").pullEvents()
        // Then
        updatedCpuBattleBrain.nextBattleUnitId() shouldBe "unit-2"
        events.shouldContainExactly(CpuBattleUnitDone(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `MarkDone should fail when the battle unit decision is still awaited`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val markingDone = { cpuBattleBrain.markDone("unit-1") }
        // Then
        shouldThrow<AlreadyAwaitingADecision> { markingDone() }
    }

    @Test
    fun `HasReachedDecisionLimit should reach the decision limit when the maximum decisions per player turn have been taken`() {
        // Given
        var cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("unit-1"))
        repeat(CpuBattleBrain.MAX_DECISIONS_PER_PLAYER_TURN) {
            cpuBattleBrain =
                cpuBattleBrain
                    .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                    .resolveCast(battleUnitId = "unit-1", abilityId = "sword")
        }
        // When
        val hasReachedDecisionLimit = cpuBattleBrain.hasReachedDecisionLimit()
        // Then
        hasReachedDecisionLimit.shouldBeTrue()
    }

    @Test
    fun `FinishPlayerTurn should have no battle units left when the player turn is finished`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.finishPlayerTurn().pullEvents()
        // Then
        updatedCpuBattleBrain.nextBattleUnitId().shouldBeNull()
        events.shouldContainExactly(CpuPlayerTurnFinished(playerId = "cpu"))
    }

    @Test
    fun `AddBattleUnit should leave the battle unit pending after the ones already pending when the player turn is being played`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("goat-1", "rat-1")).requestCast(battleUnitId = "goat-1", abilityId = "bat")
        // When
        val joinedCpuBattleBrain = cpuBattleBrain.addBattleUnit("bat-1")
        // Then
        joinedCpuBattleBrain.toDto().pendingBattleUnitIds.shouldContainExactly("goat-1", "rat-1", "bat-1")
        joinedCpuBattleBrain.toDto().isAwaitingDecision.shouldBeTrue()
    }

    @Test
    fun `AddBattleUnit should not leave the battle unit pending twice`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("goat-1", "bat-1"))
        // When
        val joinedCpuBattleBrain = cpuBattleBrain.addBattleUnit("bat-1")
        // Then
        joinedCpuBattleBrain.toDto().pendingBattleUnitIds.shouldContainExactly("goat-1", "bat-1")
    }

    @Test
    fun `AddBattleUnit should not leave the battle unit pending once the player turn is over`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("goat-1")).finishPlayerTurn().pullEvents().second
        // When
        val joinedCpuBattleBrain = cpuBattleBrain.addBattleUnit("bat-1")
        // Then
        joinedCpuBattleBrain.toDto().pendingBattleUnitIds.shouldBeEmpty()
        joinedCpuBattleBrain.isPlayingPlayerTurn().shouldBeFalse()
    }
}
