package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBrain.domain.CpuBrainMother.cpuBrain
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResolveCpuBattleUnitMoveTest {
    private val cpuBrainRepository = InMemoryCpuBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveCpuBattleUnitMove = ResolveCpuBattleUnitMove(cpuBrainRepository, eventBus)

    @Test
    fun `should continue the cpu turn when the awaited move has been applied`() {
        // Given
        cpuBrainRepository.save(cpuBrain().requestMove(battleUnitId = "unit-1", row = 2, column = 3))
        // When
        resolveCpuBattleUnitMove(battleUnitId = "unit-1", row = 2, column = 3)
        // Then
        assertThat(eventBus).hasPublishedEvents(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        assertThat(cpuBrainRepository.search()?.toDto()?.isAwaitingAction).isFalse
    }

    @Test
    fun `should ignore the move when it is not the awaited one`() {
        // Given
        cpuBrainRepository.save(cpuBrain().requestMove(battleUnitId = "unit-1", row = 2, column = 3))
        // When
        resolveCpuBattleUnitMove(battleUnitId = "human-unit", row = 2, column = 3)
        // Then
        assertThat(eventBus).hasNotPublishedEvents()
        assertThat(cpuBrainRepository.search()?.toDto()?.isAwaitingAction).isTrue
    }

    @Test
    fun `should ignore the move when there is no cpu turn in progress`() {
        // Given
        // When
        resolveCpuBattleUnitMove(battleUnitId = "unit-1", row = 2, column = 3)
        // Then
        assertThat(eventBus).hasNotPublishedEvents()
    }
}
