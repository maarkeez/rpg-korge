package com.mkz.rpg.cpuBattleBrain.domain

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.AlreadyAwaitingAnAction
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.NoBattleUnitsToManage
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainError.NotTheNextBattleUnit
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleBrainInitialized
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuTurnFinished
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
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
    fun `StartTurn should manage the battle units in order when the turn starts`() {
        // Given
        val battleUnitIds = listOf("unit-1", "unit-2")
        // When
        val (events, cpuBattleBrain) = CpuBattleBrain.startTurn(playerId = "cpu", battleUnitIds = battleUnitIds).pullEvents()
        // Then
        cpuBattleBrain.toDto() shouldBe CpuBattleBrain.Dto(playerId = "cpu", pendingBattleUnitIds = battleUnitIds, isAwaitingAction = false, actionsTaken = 0)
        cpuBattleBrain.nextBattleUnitId() shouldBe "unit-1"
        events.shouldContainExactly(CpuBattleBrainInitialized(playerId = "cpu"))
    }

    @Test
    fun `StartTurn should fail when there are no battle units to manage`() {
        // Given
        val battleUnitIds = emptyList<String>()
        // When
        val start = { CpuBattleBrain.startTurn(playerId = "cpu", battleUnitIds = battleUnitIds) }
        // Then
        shouldThrow<NoBattleUnitsToManage> { start() }
    }

    @Test
    fun `RequestMove should await the move and count the action when the next battle unit moves`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.requestMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeTrue()
        updatedCpuBattleBrain.toDto().actionsTaken shouldBe 1
        events.shouldBeEmpty()
    }

    @Test
    fun `RequestMove should fail when the battle unit is not the next one to manage`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val move = { cpuBattleBrain.requestMove(battleUnitId = "unit-2", row = 1, column = 2) }
        // Then
        shouldThrow<NotTheNextBattleUnit> { move() }
    }

    @Test
    fun `RequestMove should fail when the previous action is still awaited`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val move = { cpuBattleBrain.requestMove(battleUnitId = "unit-1", row = 1, column = 2) }
        // Then
        shouldThrow<AlreadyAwaitingAnAction> { move() }
    }

    @Test
    fun `RequestCast should await the cast and count the action when the next battle unit casts`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val updatedCpuBattleBrain = cpuBattleBrain.requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeTrue()
        updatedCpuBattleBrain.toDto().actionsTaken shouldBe 1
    }

    @Test
    fun `ResolveMove should continue when the awaited move reaches the requested tile`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestMove(battleUnitId = "unit-1", row = 1, column = 2)
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeFalse()
        events.shouldContainExactly(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `ResolveMove should keep waiting when another tile is occupied`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestMove(battleUnitId = "unit-1", row = 1, column = 2)
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveMove(battleUnitId = "unit-1", row = 5, column = 5).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `ResolveMove should keep waiting when a cast is awaited`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "teleport")
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `ResolveCast should continue when the awaited cast is resolved`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveCast(battleUnitId = "unit-1", abilityId = "sword").pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeFalse()
        events.shouldContainExactly(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `ResolveCast should keep waiting when another battle unit cast is resolved`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveCast(battleUnitId = "unit-2", abilityId = "sword").pullEvents()
        // Then
        updatedCpuBattleBrain.toDto().isAwaitingAction.shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `ManageBattleUnit should continue with the next battle unit when the battle unit is managed`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.manageBattleUnit("unit-1").pullEvents()
        // Then
        updatedCpuBattleBrain.nextBattleUnitId() shouldBe "unit-2"
        events.shouldContainExactly(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `ManageBattleUnit should fail when the battle unit action is still awaited`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
        // When
        val managing = { cpuBattleBrain.manageBattleUnit("unit-1") }
        // Then
        shouldThrow<AlreadyAwaitingAnAction> { managing() }
    }

    @Test
    fun `HasReachedActionLimit should reach the action limit when the maximum actions per turn have been taken`() {
        // Given
        var cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("unit-1"))
        repeat(CpuBattleBrain.MAX_ACTIONS_PER_TURN) {
            cpuBattleBrain =
                cpuBattleBrain
                    .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                    .resolveCast(battleUnitId = "unit-1", abilityId = "sword")
        }
        // When
        val hasReachedActionLimit = cpuBattleBrain.hasReachedActionLimit()
        // Then
        hasReachedActionLimit.shouldBeTrue()
    }

    @Test
    fun `FinishTurn should have no battle units left when the turn is finished`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain()
        // When
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.finishTurn().pullEvents()
        // Then
        updatedCpuBattleBrain.nextBattleUnitId().shouldBeNull()
        events.shouldContainExactly(CpuTurnFinished(playerId = "cpu"))
    }

    @Test
    fun `AddBattleUnit should manage the battle unit after the ones already pending when the turn is being played`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("goat-1", "rat-1")).requestCast(battleUnitId = "goat-1", abilityId = "bat")
        // When
        val joinedCpuBattleBrain = cpuBattleBrain.addBattleUnit("bat-1")
        // Then
        joinedCpuBattleBrain.toDto().pendingBattleUnitIds.shouldContainExactly("goat-1", "rat-1", "bat-1")
        joinedCpuBattleBrain.toDto().isAwaitingAction.shouldBeTrue()
    }

    @Test
    fun `AddBattleUnit should not manage the battle unit twice`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("goat-1", "bat-1"))
        // When
        val joinedCpuBattleBrain = cpuBattleBrain.addBattleUnit("bat-1")
        // Then
        joinedCpuBattleBrain.toDto().pendingBattleUnitIds.shouldContainExactly("goat-1", "bat-1")
    }

    @Test
    fun `AddBattleUnit should not manage the battle unit once the turn is over`() {
        // Given
        val cpuBattleBrain = cpuBattleBrain(battleUnitIds = listOf("goat-1")).finishTurn().pullEvents().second
        // When
        val joinedCpuBattleBrain = cpuBattleBrain.addBattleUnit("bat-1")
        // Then
        joinedCpuBattleBrain.toDto().pendingBattleUnitIds.shouldBeEmpty()
        joinedCpuBattleBrain.isPlayingTurn().shouldBeFalse()
    }
}
