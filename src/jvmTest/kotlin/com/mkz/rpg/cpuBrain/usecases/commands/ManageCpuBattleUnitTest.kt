package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.domain.CpuBrain
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuTurnFinished
import com.mkz.rpg.cpuBrain.domain.CpuBrainMother.cpuBrain
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast.CastDecision
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ManageCpuBattleUnitTest {
    private val cpuBrainRepository = InMemoryCpuBrainRepository()
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val whatShouldCast: WhatShouldCast = mock()
    private val whereShouldMove: WhereShouldMove = mock()
    private val eventBus = FakeEventBus()
    private val manageCpuBattleUnit =
        ManageCpuBattleUnit(
            cpuBrainRepository = cpuBrainRepository,
            searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
            whatShouldCast = whatShouldCast,
            whereShouldMove = whereShouldMove,
            eventBus = eventBus,
        )

    private val castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(row = 0, column = 1)))

    @BeforeEach
    fun setup() {
        whenever(searchBattleUnitsByPlayerId("cpu")).thenReturn(listOf(battleUnit(id = "unit-1").toDto(), battleUnit(id = "unit-2").toDto()))
    }

    @Test
    fun `should cast and await the cast when the next battle unit should cast`() {
        // Given
        cpuBrainRepository.save(cpuBrain())
        whenever(whatShouldCast("unit-1")).thenReturn(CastDecision(abilityId = "sword", castGroup = castGroup))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestCastAbility(battleUnitId = "unit-1", abilityId = "sword", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1))),
        )
        assertThat(cpuBrainRepository.search()?.toDto()?.isAwaitingAction).isTrue
    }

    @Test
    fun `should move and await the move when the next battle unit should move and not cast`() {
        // Given
        cpuBrainRepository.save(cpuBrain())
        whenever(whatShouldCast("unit-1")).thenReturn(null)
        whenever(whereShouldMove("unit-1")).thenReturn(Battlefield.Dto.PositionDto(row = 2, column = 3))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasPublishedEvents(BattleUnitEvent.RequestMoveBattleUnit(battleUnitId = "unit-1", moveToRow = 2, moveToColumn = 3))
        assertThat(cpuBrainRepository.search()?.toDto()?.isAwaitingAction).isTrue
    }

    @Test
    fun `should manage the battle unit when it has nothing left to do`() {
        // Given
        cpuBrainRepository.save(cpuBrain())
        whenever(whatShouldCast("unit-1")).thenReturn(null)
        whenever(whereShouldMove("unit-1")).thenReturn(null)
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasPublishedEvents(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
        assertThat(cpuBrainRepository.search()?.nextBattleUnitId()).isEqualTo("unit-2")
    }

    @Test
    fun `should manage the battle unit without acting when it has been defeated`() {
        // Given
        cpuBrainRepository.save(cpuBrain())
        whenever(searchBattleUnitsByPlayerId("cpu")).thenReturn(listOf(battleUnit(id = "unit-2").toDto()))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasPublishedEvents(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `should finish the turn when every battle unit has been managed`() {
        // Given
        cpuBrainRepository.save(cpuBrain(battleUnitIds = listOf("unit-1")).manageBattleUnit("unit-1").pullEvents().second)
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasPublishedEvents(CpuTurnFinished(playerId = "cpu"), BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should finish the turn when the action limit has been reached`() {
        // Given
        var limitedCpuBrain = cpuBrain()
        repeat(CpuBrain.MAX_ACTIONS_PER_TURN) {
            limitedCpuBrain =
                limitedCpuBrain
                    .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                    .resolveCast(battleUnitId = "unit-1", abilityId = "sword")
        }
        cpuBrainRepository.save(limitedCpuBrain.pullEvents().second)
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasPublishedEvents(CpuTurnFinished(playerId = "cpu"), BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should not act when the cpu brain is awaiting an action`() {
        // Given
        cpuBrainRepository.save(cpuBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasNotPublishedEvents()
    }

    @Test
    fun `should not act when the cpu brain belongs to another player`() {
        // Given
        cpuBrainRepository.save(cpuBrain(playerId = "another-cpu"))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        assertThat(eventBus).hasNotPublishedEvents()
    }
}
