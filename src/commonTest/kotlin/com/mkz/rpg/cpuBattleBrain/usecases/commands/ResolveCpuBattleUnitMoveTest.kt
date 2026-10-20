package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitActionResolved
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResolveCpuBattleUnitMoveTest {
    private val cpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveCpuBattleUnitMove = ResolveCpuBattleUnitMove(cpuBattleBrainRepository, eventBus)

    @Test
    fun `should continue the cpu turn when the awaited move has been applied`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestMove(battleUnitId = "unit-1", row = 2, column = 3))
        // When
        resolveCpuBattleUnitMove(battleUnitId = "unit-1", row = 2, column = 3)
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingAction
            .shouldBeFalse()
    }

    @Test
    fun `should ignore the move when it is not the awaited one`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestMove(battleUnitId = "unit-1", row = 2, column = 3))
        // When
        resolveCpuBattleUnitMove(battleUnitId = "human-unit", row = 2, column = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingAction
            .shouldBeTrue()
    }

    @Test
    fun `should ignore the move when there is no cpu turn in progress`() {
        // Given
        // When
        resolveCpuBattleUnitMove(battleUnitId = "unit-1", row = 2, column = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
