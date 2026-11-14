package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import kotlin.test.Test

class SwapBattleUnitsTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val swapBattleUnits =
        SwapBattleUnits(
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
            searchPosition = queries.searchPosition,
        )

    @Test
    fun `should walk both battle units onto each other's tile and request the battlefield to swap them when both are deployed`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2"))
        queries.battlefieldRepository.create(battlefield("K\nL", occupants = mapOf('K' to "knight-1", 'L' to "knight-2")))
        // When
        swapBattleUnits(battleUnitId = "knight-1", otherBattleUnitId = "knight-2")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.BattleUnitMoved("knight-1", 0, 0, 1, 0, listOf(PositionDto(1, 0)), swappedWithBattleUnitId = "knight-2"),
            BattleUnitEvent.BattleUnitMoved("knight-2", 1, 0, 0, 0, listOf(PositionDto(0, 0)), swappedWithBattleUnitId = "knight-1"),
            BattlefieldEvent.RequestSwapOccupants(occupantId = "knight-1", otherOccupantId = "knight-2"),
        )
    }

    @Test
    fun `should not swap when a battle unit is not deployed`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2"))
        queries.battlefieldRepository.create(battlefield("K\n.", occupants = mapOf('K' to "knight-1")))
        // When
        swapBattleUnits(battleUnitId = "knight-1", otherBattleUnitId = "knight-2")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
