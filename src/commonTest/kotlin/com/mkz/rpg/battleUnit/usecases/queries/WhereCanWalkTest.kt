package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class WhereCanWalkTest {
    private val queries = InMemoryQueries()
    private val whereCanWalk = queries.whereCanWalk

    @Test
    fun `should return positions that can be walked to when the battle unit can walk`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(maximumSteps = 2).toDto()))
        // The tiles behind the wall are in range, but too far to walk to.
        queries.battlefieldRepository.create(
            battlefield(
                """
                K # .
                . # .
                . . .
                """,
                occupants = mapOf('K' to "knight-1"),
            ),
        )
        // When
        val result = whereCanWalk("knight-1")
        // Then
        result.shouldContainExactlyInAnyOrder(PositionDto(1, 0), PositionDto(2, 0))
    }

    @Test
    fun `should return empty list when the battle unit does not exist`() {
        // Given
        // When
        val result = whereCanWalk("unknown-battle-unit")
        // Then
        result.shouldBeEmpty()
    }
}
