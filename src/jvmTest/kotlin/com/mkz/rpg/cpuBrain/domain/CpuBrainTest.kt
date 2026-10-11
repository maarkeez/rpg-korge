package com.mkz.rpg.cpuBrain.domain

import com.mkz.rpg.cpuBrain.domain.CpuBrainError.AlreadyAwaitingAnAction
import com.mkz.rpg.cpuBrain.domain.CpuBrainError.NoBattleUnitsToManage
import com.mkz.rpg.cpuBrain.domain.CpuBrainError.NotTheNextBattleUnit
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBrainInitialized
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuTurnFinished
import com.mkz.rpg.cpuBrain.domain.CpuBrainMother.cpuBrain
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
            assertThat(cpuBrain.toDto()).isEqualTo(CpuBrain.Dto(playerId = "cpu", pendingBattleUnitIds = battleUnitIds, isAwaitingAction = false, actionsTaken = 0))
            assertThat(cpuBrain.nextBattleUnitId()).isEqualTo("unit-1")
            assertThat(events).containsExactly(CpuBrainInitialized(playerId = "cpu"))
        }

        @Test
        fun `should fail when there are no battle units to manage`() {
            // Given
            val battleUnitIds = emptyList<String>()
            // When
            val start = { CpuBrain.startTurn(playerId = "cpu", battleUnitIds = battleUnitIds) }
            // Then
            assertThatThrownBy { start() }.isInstanceOf(NoBattleUnitsToManage::class.java)
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
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isTrue
            assertThat(updatedCpuBrain.toDto().actionsTaken).isEqualTo(1)
            assertThat(events).isEmpty()
        }

        @Test
        fun `should fail when the battle unit is not the next one to manage`() {
            // Given
            val cpuBrain = cpuBrain()
            // When
            val move = { cpuBrain.requestMove(battleUnitId = "unit-2", row = 1, column = 2) }
            // Then
            assertThatThrownBy { move() }.isInstanceOf(NotTheNextBattleUnit::class.java)
        }

        @Test
        fun `should fail when the previous action is still awaited`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val move = { cpuBrain.requestMove(battleUnitId = "unit-1", row = 1, column = 2) }
            // Then
            assertThatThrownBy { move() }.isInstanceOf(AlreadyAwaitingAnAction::class.java)
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
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isTrue
            assertThat(updatedCpuBrain.toDto().actionsTaken).isEqualTo(1)
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
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isFalse
            assertThat(events).containsExactly(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        }

        @Test
        fun `should keep waiting when another tile is occupied`() {
            // Given
            val cpuBrain = cpuBrain().requestMove(battleUnitId = "unit-1", row = 1, column = 2)
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveMove(battleUnitId = "unit-1", row = 5, column = 5).pullEvents()
            // Then
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isTrue
            assertThat(events).isEmpty()
        }

        @Test
        fun `should keep waiting when a cast is awaited`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "teleport")
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveMove(battleUnitId = "unit-1", row = 1, column = 2).pullEvents()
            // Then
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isTrue
            assertThat(events).isEmpty()
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
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isFalse
            assertThat(events).containsExactly(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        }

        @Test
        fun `should keep waiting when another battle unit cast is resolved`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val (events, updatedCpuBrain) = cpuBrain.resolveCast(battleUnitId = "unit-2", abilityId = "sword").pullEvents()
            // Then
            assertThat(updatedCpuBrain.toDto().isAwaitingAction).isTrue
            assertThat(events).isEmpty()
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
            assertThat(updatedCpuBrain.nextBattleUnitId()).isEqualTo("unit-2")
            assertThat(events).containsExactly(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
        }

        @Test
        fun `should fail when the battle unit action is still awaited`() {
            // Given
            val cpuBrain = cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword")
            // When
            val managing = { cpuBrain.manageBattleUnit("unit-1") }
            // Then
            assertThatThrownBy { managing() }.isInstanceOf(AlreadyAwaitingAnAction::class.java)
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
            assertThat(hasReachedActionLimit).isTrue
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
            assertThat(updatedCpuBrain.nextBattleUnitId()).isNull()
            assertThat(events).containsExactly(CpuTurnFinished(playerId = "cpu"))
        }
    }
}
