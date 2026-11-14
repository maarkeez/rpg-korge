package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.services

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class WalkingRangeServiceTest {
    private val queries = InMemoryQueries()
    private val walkingRangeService =
        WalkingRangeService(
            searchBattleUnitById = queries.searchBattleUnitById,
            searchVacantTiles = queries.searchVacantTiles,
            canWalkTo = queries.canWalkTo,
        )

    @Test
    fun `should return only the tiles the battle unit can move to when computing the tiles where it can move`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(maximumSteps = 2).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K . .
                # # .
                """,
                occupants = mapOf('K' to "battle-unit-1"),
            ),
        )
        // When
        val walkingRange = walkingRangeService.walkingRange("battle-unit-1")
        // Then
        walkingRange.shouldContainExactly(
            TileDto(row = 0, column = 1),
            TileDto(row = 0, column = 2),
        )
    }

    @Test
    fun `should return an empty set when there are no vacant tiles`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1"))
        queries.battlefieldRepository.create(battlefield("K #", occupants = mapOf('K' to "battle-unit-1")))
        // When
        val walkingRange = walkingRangeService.walkingRange("battle-unit-1")
        // Then
        walkingRange.shouldBeEmpty()
    }
}
