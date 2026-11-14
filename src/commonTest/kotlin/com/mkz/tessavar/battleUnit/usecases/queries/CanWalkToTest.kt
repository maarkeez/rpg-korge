package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CanWalkToTest {
    private val queries = InMemoryQueries()
    private val canWalkTo = queries.canWalkTo

    @Test
    fun `should return true when the shortest path is within the remaining steps`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(maximumSteps = 3).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K
                .
                .
                .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        val result = canWalkTo(battleUnitId = "knight-1", walkToRow = 3, walkToColumn = 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the shortest path exceeds the remaining steps`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(maximumSteps = 2).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K
                .
                .
                .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        val result = canWalkTo(battleUnitId = "knight-1", walkToRow = 3, walkToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when there is no path to the destination`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(maximumSteps = 3).toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K
                #
                #
                .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        val result = canWalkTo(battleUnitId = "knight-1", walkToRow = 3, walkToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val result = canWalkTo(battleUnitId = "unknown-battle-unit", walkToRow = 0, walkToColumn = 1)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the position is not found`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val result = canWalkTo(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 1)
        // Then
        result.shouldBeFalse()
    }
}
