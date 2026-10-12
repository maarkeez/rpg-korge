package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CanMoveToTest {
    private val queries = InMemoryQueries()
    private val canMoveTo = queries.canMoveTo

    @Test
    fun `should return true when the shortest path is within the remaining steps`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 3).toDto()))
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
        val result = canMoveTo(battleUnitId = "knight-1", moveToRow = 3, moveToColumn = 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the shortest path exceeds the remaining steps`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 2).toDto()))
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
        val result = canMoveTo(battleUnitId = "knight-1", moveToRow = 3, moveToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when there is no path to the destination`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(movementRange = 3).toDto()))
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
        val result = canMoveTo(battleUnitId = "knight-1", moveToRow = 3, moveToColumn = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val result = canMoveTo(battleUnitId = "unknown-battle-unit", moveToRow = 0, moveToColumn = 1)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the position is not found`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val result = canMoveTo(battleUnitId = "knight-1", moveToRow = 0, moveToColumn = 1)
        // Then
        result.shouldBeFalse()
    }
}
