package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class SearchTilesThatCanBeOccupiedTest {
    private val queries = InMemoryQueries()
    private val searchTilesThatCanBeOccupied = queries.searchTilesThatCanBeOccupied

    @Test
    fun `should return tiles that can be occupied when the battle unit is deployed`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . . .
                . K . .
                . . # .
                """,
                occupants = mapOf('K' to "battle-unit-1"),
            ),
        )
        // When
        val result = searchTilesThatCanBeOccupied("battle-unit-1", distance = 1)
        // Then
        result.shouldContainExactlyInAnyOrder(
            PositionDto(0, 0),
            PositionDto(0, 1),
            PositionDto(0, 2),
            PositionDto(1, 0),
            PositionDto(1, 2),
            PositionDto(2, 0),
            PositionDto(2, 1),
        )
    }

    @Test
    fun `should return empty list when distance is less than or equal to zero`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . .
                . K .
                . . .
                """,
                occupants = mapOf('K' to "battle-unit-1"),
            ),
        )
        // When
        val result = searchTilesThatCanBeOccupied("battle-unit-1", distance = 0)
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return empty list when no battlefield exists`() {
        // Given
        // When
        val result = searchTilesThatCanBeOccupied("battle-unit-1", distance = 1)
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return empty list when the battle unit is not deployed`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . ."))
        // When
        val result = searchTilesThatCanBeOccupied("unknown-battle-unit", distance = 1)
        // Then
        result.shouldBeEmpty()
    }
}
