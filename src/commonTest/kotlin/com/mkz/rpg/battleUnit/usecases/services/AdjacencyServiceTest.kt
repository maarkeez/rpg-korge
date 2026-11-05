package com.mkz.rpg.battleUnit.usecases.services

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AdjacencyServiceTest {
    private val adjacencyService = AdjacencyService()

    @Test
    fun `IsAdjacent should be true when the tiles share a side`() {
        // Given
        // When
        val isAdjacent = adjacencyService.isAdjacent(fromRow = 2, fromColumn = 2, toRow = 2, toColumn = 3)
        // Then
        isAdjacent shouldBe true
    }

    @Test
    fun `IsAdjacent should be false when the tiles are diagonal`() {
        // Given
        // When
        val isAdjacent = adjacencyService.isAdjacent(fromRow = 2, fromColumn = 2, toRow = 3, toColumn = 3)
        // Then
        isAdjacent shouldBe false
    }

    @Test
    fun `IsAdjacent should be false when it is the same tile`() {
        // Given
        // When
        val isAdjacent = adjacencyService.isAdjacent(fromRow = 2, fromColumn = 2, toRow = 2, toColumn = 2)
        // Then
        isAdjacent shouldBe false
    }
}
