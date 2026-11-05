package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchVacantTilesTest {
    private val queries = InMemoryQueries()
    private val searchVacantTiles = queries.searchVacantTiles

    @Test
    fun `should return vacant tiles when the battle unit is deployed`() {
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
        val result = searchVacantTiles("battle-unit-1", tilesAround = 1)
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
    fun `should return empty list when tiles around is less than or equal to zero`() {
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
        val result = searchVacantTiles("battle-unit-1", tilesAround = 0)
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return empty list when no battlefield exists`() {
        // Given
        // When
        val result = searchVacantTiles("battle-unit-1", tilesAround = 1)
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return empty list when the battle unit is not deployed`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . ."))
        // When
        val result = searchVacantTiles("unknown-battle-unit", tilesAround = 1)
        // Then
        result.shouldBeEmpty()
    }
}
