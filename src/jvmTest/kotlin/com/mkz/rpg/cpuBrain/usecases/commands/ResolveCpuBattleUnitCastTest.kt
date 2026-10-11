package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBrain.domain.CpuBrainMother.cpuBrain
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResolveCpuBattleUnitCastTest {
    private val cpuBrainRepository = InMemoryCpuBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveCpuBattleUnitCast = ResolveCpuBattleUnitCast(cpuBrainRepository, eventBus)

    @Test
    fun `should continue the cpu turn when the awaited cast has been resolved`() {
        // Given
        cpuBrainRepository.save(cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        resolveCpuBattleUnitCast(battleUnitId = "unit-1", abilityId = "sword")
        // Then
        assertThat(eventBus).hasPublishedEvents(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        assertThat(cpuBrainRepository.search()?.toDto()?.isAwaitingAction).isFalse
    }

    @Test
    fun `should ignore the cast when it is not the awaited one`() {
        // Given
        cpuBrainRepository.save(cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        resolveCpuBattleUnitCast(battleUnitId = "unit-1", abilityId = "heal")
        // Then
        assertThat(eventBus).hasNotPublishedEvents()
        assertThat(cpuBrainRepository.search()?.toDto()?.isAwaitingAction).isTrue
    }
}
