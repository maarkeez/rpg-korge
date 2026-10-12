package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test

class SearchShortestPathTest {
    private val queries = InMemoryQueries()
    private val searchShortestPath = queries.searchShortestPath

    @Test
    fun `should return the tiles walked in order when the destination is in a straight line`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                .
                .
                .
                .
                """,
            ),
        )
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)
        // Then
        path.shouldContainExactly(PositionDto(1, 0), PositionDto(2, 0), PositionDto(3, 0))
    }

    @Test
    fun `should walk around the tiles that can not be occupied when they block the straight line`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                . # .
                . . .
                """,
            ),
        )
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2)
        // Then
        path.shouldContainExactly(PositionDto(1, 0), PositionDto(1, 1), PositionDto(1, 2), PositionDto(0, 2))
    }

    @Test
    fun `should return the shortest path when there are several paths`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . .
                . # .
                . . .
                """,
            ),
        )
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 1, toColumn = 2)
        // Then
        path.shouldContainExactly(PositionDto(0, 1), PositionDto(0, 2), PositionDto(1, 2))
    }

    @Test
    fun `should return null when the destination can only be reached jumping over tiles that can not be occupied`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                .
                #
                #
                .
                """,
            ),
        )
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)
        // Then
        path.shouldBeNull()
    }

    @Test
    fun `should return an empty path when the destination is the origin`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 1, toRow = 0, toColumn = 1)
        // Then
        path.shouldBeEmpty()
    }
}
