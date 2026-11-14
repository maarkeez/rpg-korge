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

class ResolveCpuBattleUnitWalkTest {
    private val cpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveCpuBattleUnitWalk = ResolveCpuBattleUnitWalk(cpuBattleBrainRepository, eventBus)

    @Test
    fun `should continue the cpu player turn when the awaited walk has ended`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestWalk(battleUnitId = "unit-1", row = 2, column = 3))
        // When
        resolveCpuBattleUnitWalk(battleUnitId = "unit-1", row = 2, column = 3)
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitDecisionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingDecision
            .shouldBeFalse()
    }

    @Test
    fun `should ignore the walk when it is not the awaited one`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestWalk(battleUnitId = "unit-1", row = 2, column = 3))
        // When
        resolveCpuBattleUnitWalk(battleUnitId = "human-unit", row = 2, column = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingDecision
            .shouldBeTrue()
    }

    @Test
    fun `should ignore the walk when there is no cpu player turn in progress`() {
        // Given
        // When
        resolveCpuBattleUnitWalk(battleUnitId = "unit-1", row = 2, column = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
