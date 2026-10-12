package com.mkz.rpg.cpuBrain.domain

import com.mkz.rpg.cpuBrain.domain.CpuBrainError.AlreadyAwaitingAnAction
import com.mkz.rpg.cpuBrain.domain.CpuBrainError.NoBattleUnitsToManage
import com.mkz.rpg.cpuBrain.domain.CpuBrainError.NotTheNextBattleUnit
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBrainInitialized
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuTurnFinished
import com.mkz.rpg.cpuBrain.domain.CpuBrainMother.cpuBrain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CpuBrainTest {
    @Nested
    inner class StartTurn {
        @Test
        fun `should manage the battle units in order when the turn starts`() {
            // Given
            val battleUnitIds = listOf("unit-1", "unit-2")
            // When
            val (events, cpuBrain) = CpuBrain.startTurn(playerId = "cpu", battleUnitIds = battleUnitIds).pullEvents()
            // Then
            cpuBrain.toDto() shouldBe CpuBrain.Dto(playerId = "cpu", pendingBattleUnitIds = battleUnitIds, isAwaitingAction = false, actionsTaken = 0)
            cpuBrain.nextBattleUnitId() shouldBe "unit-1"
            events.shouldContainExactly(CpuBrainInitialized(playerId = "cpu"))
        }

        @Test
        fun `should fail when there are no battle units to manage`() {
            // Given
            val battleUnitIds = emptyList<String>()
            // When
            val start = { CpuBrain.startTurn(playerId = "cpu", battleUnitIds = battleUnitIds) }
            // Then
            shouldThrow<NoBattleUnitsToManage> { start() }
        }
    }

    @Nested
    inner class RequestMove {
        @Test
        fun `should await the move and count the action when the next battle unit moves`() {
            // Given
            val cpuBrain = cpuBrain()
            // When
            val (events, updatedCpuBrain) = cpuBrain.requestMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeTrue()
            updatedCpuBrain.toDto().actionsTaken shouldBe 1
            events.shouldBeEmpty()
        }

        @Test
        fun `should fail when the battle unit is not the next one to manage`() {
            // Given
            val cpuBrain = cpuBrain()
            // When
            val move = { cpuBrain.requestMove(battleUnitId = "unit-2", row = 1, column = 2) }
            // Then
            shouldThrow<NotTheNextBattleUnit> { move() }
        }

        @Test
        fun `should fail when the previous action is still awaited`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val move = { cpuBrain.requestMove(battleUnitId = "unit-1", row = 1, column = 2) }
            // Then
            shouldThrow<AlreadyAwaitingAnAction> { move() }
        }
    }

    @Nested
    inner class RequestCast {
        @Test
        fun `should await the cast and count the action when the next battle unit casts`() {
            // Given
            val cpuBrain = cpuBrain()
            // When
            val updatedCpuBrain = cpuBrain.requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeTrue()
            updatedCpuBrain.toDto().actionsTaken shouldBe 1
        }
    }

    @Nested
    inner class ResolveMove {
        @Test
        fun `should continue when the awaited move reaches the requested tile`() {
            // Given
            val cpuBrain = cpuBrain().requestMove(battleUnitId = "unit-1", row = 1, column = 2)
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeFalse()
            events.shouldContainExactly(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        }

        @Test
        fun `should keep waiting when another tile is occupied`() {
            // Given
            val cpuBrain = cpuBrain().requestMove(battleUnitId = "unit-1", row = 1, column = 2)
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveMove(battleUnitId = "unit-1", row = 5, column = 5).pullEvents()
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeTrue()
            events.shouldBeEmpty()
        }

        @Test
        fun `should keep waiting when a cast is awaited`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "teleport")
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeTrue()
            events.shouldBeEmpty()
        }
    }

    @Nested
    inner class ResolveCast {
        @Test
        fun `should continue when the awaited cast is resolved`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveCast(battleUnitId = "unit-1", abilityId = "sword").pullEvents()
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeFalse()
            events.shouldContainExactly(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        }

        @Test
        fun `should keep waiting when another battle unit cast is resolved`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveCast(battleUnitId = "unit-2", abilityId = "sword").pullEvents()
            // Then
            updatedCpuBrain.toDto().isAwaitingAction.shouldBeTrue()
            events.shouldBeEmpty()
        }
    }

    @Nested
    inner class ManageBattleUnit {
        @Test
        fun `should continue with the next battle unit when the battle unit is managed`() {
            // Given
            val cpuBrain = cpuBrain()
            // When
            val (events, updatedCpuBrain) = cpuBrain.manageBattleUnit("unit-1").pullEvents()
            // Then
            updatedCpuBrain.nextBattleUnitId() shouldBe "unit-2"
            events.shouldContainExactly(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
        }

        @Test
        fun `should fail when the battle unit action is still awaited`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val managing = { cpuBrain.manageBattleUnit("unit-1") }
            // Then
            shouldThrow<AlreadyAwaitingAnAction> { managing() }
        }
    }

    @Nested
    inner class HasReachedActionLimit {
        @Test
        fun `should reach the action limit when the maximum actions per turn have been taken`() {
            // Given
            var cpuBrain = cpuBrain(battleUnitIds = listOf("unit-1"))
            repeat(CpuBrain.MAX_ACTIONS_PER_TURN) {
                cpuBrain =
                    cpuBrain
                        .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                        .resolveCast(battleUnitId = "unit-1", abilityId = "sword")
            }
            // When
            val hasReachedActionLimit = cpuBrain.hasReachedActionLimit()
            // Then
            hasReachedActionLimit.shouldBeTrue()
        }
    }

    @Nested
    inner class FinishTurn {
        @Test
        fun `should have no battle units left when the turn is finished`() {
            // Given
            val cpuBrain = cpuBrain()
            // When
            val (events, updatedCpuBrain) = cpuBrain.finishTurn().pullEvents()
            // Then
            updatedCpuBrain.nextBattleUnitId().shouldBeNull()
            events.shouldContainExactly(CpuTurnFinished(playerId = "cpu"))
        }
    }
}
