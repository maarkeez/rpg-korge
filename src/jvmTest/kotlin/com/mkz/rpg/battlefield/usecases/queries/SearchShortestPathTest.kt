package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchShortestPathTest {
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val searchShortestPath = SearchShortestPath(canBattlefieldTileBeOccupied)

    private fun occupiableTiles(vararg positions: PositionDto) {
        whenever(canBattlefieldTileBeOccupied(any(), any())).thenReturn(false)
        positions.forEach { position -> whenever(canBattlefieldTileBeOccupied(position.row, position.column)).thenReturn(true) }
    }

    @Test
    fun `should return the tiles walked in order when the destination is in a straight line`() {
        // Given
        occupiableTiles(PositionDto(1, 0), PositionDto(2, 0), PositionDto(3, 0))
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)
        // Then
        assertThat(path).containsExactly(PositionDto(1, 0), PositionDto(2, 0), PositionDto(3, 0))
    }

    @Test
    fun `should walk around the tiles that can not be occupied when they block the straight line`() {
        // Given
        occupiableTiles(PositionDto(1, 0), PositionDto(1, 1), PositionDto(1, 2), PositionDto(0, 2))
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2)
        // Then
        assertThat(path).containsExactly(PositionDto(1, 0), PositionDto(1, 1), PositionDto(1, 2), PositionDto(0, 2))
    }

    @Test
    fun `should return the shortest path when there are several paths`() {
        // Given
        occupiableTiles(PositionDto(0, 1), PositionDto(0, 2), PositionDto(1, 0), PositionDto(2, 0), PositionDto(2, 1), PositionDto(2, 2), PositionDto(1, 2))
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 1, toColumn = 2)
        // Then
        assertThat(path).containsExactly(PositionDto(0, 1), PositionDto(0, 2), PositionDto(1, 2))
    }

    @Test
    fun `should return null when the destination can only be reached jumping over tiles that can not be occupied`() {
        // Given
        occupiableTiles(PositionDto(3, 0))
        // When
        val path = searchShortestPath(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 0)
        // Then
        assertThat(path).isNull()
    }

    @Test
    fun `should return an empty path when the destination is the origin`() {
        // Given
        occupiableTiles()
        // When
        val path = searchShortestPath(fromRow = 2, fromColumn = 2, toRow = 2, toColumn = 2)
        // Then
        assertThat(path).isEmpty()
    }
}
