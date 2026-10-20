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

class ResolveCpuBattleUnitCastTest {
    private val cpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()
    private val eventBus = FakeEventBus()
    private val resolveCpuBattleUnitCast = ResolveCpuBattleUnitCast(cpuBattleBrainRepository, eventBus)

    @Test
    fun `should continue the cpu turn when the awaited cast has been resolved`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        resolveCpuBattleUnitCast(battleUnitId = "unit-1", abilityId = "sword")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitActionResolved(playerId = "cpu", battleUnitId = "unit-1"))
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingAction
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
            ?.isAwaitingAction
            .shouldBeTrue()
    }
}
