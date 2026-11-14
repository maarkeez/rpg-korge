package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDecisionResolved
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResolveCpuBattleUnitCastTest {
    private val cpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveCpuBattleUnitCast = ResolveCpuBattleUnitCast(cpuBattleBrainRepository, eventBus)

    @Test
    fun `should continue the cpu player turn when the awaited cast has been resolved`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        resolveCpuBattleUnitCast(battleUnitId = "unit-1", abilityId = "sword")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitDecisionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingDecision
            .shouldBeFalse()
    }

    @Test
    fun `should ignore the cast when it is not the awaited one`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        resolveCpuBattleUnitCast(battleUnitId = "unit-1", abilityId = "heal")
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingDecision
            .shouldBeTrue()
    }
}
