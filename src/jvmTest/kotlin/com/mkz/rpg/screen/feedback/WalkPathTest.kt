package com.mkz.rpg.screen.feedback

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WalkPathTest {
    private val everywhere: (Int, Int) -> Boolean = { _, _ -> true }

    @Test
    fun `should walk tile by tile and end on the destination when the way is open`() {
        // Given
        val from = 0 to 0
        val to = 0 to 3
        // When
        val path = WalkPath.find(from, to, everywhere)
        // Then
        assertThat(path).containsExactly(0 to 1, 0 to 2, 0 to 3)
    }

    @Test
    fun `should go around a tile that cannot be walked when it blocks the straight way`() {
        // Given
        val wall: (Int, Int) -> Boolean = { row, column -> !(row == 0 && column == 1) && row in 0..1 && column in 0..2 }
        // When
        val path = WalkPath.find(from = 0 to 0, to = 0 to 2, isWalkable = wall)
        // Then
        assertThat(path).containsExactly(1 to 0, 1 to 1, 1 to 2, 0 to 2)
    }

    @Test
    fun `should allow the destination when it is not walkable because the unit already stands on it`() {
        // Given
        val onlyStart: (Int, Int) -> Boolean = { row, column -> row == 0 && column == 0 }
        // When
        val path = WalkPath.find(from = 0 to 0, to = 0 to 1, isWalkable = onlyStart)
        // Then
        assertThat(path).containsExactly(0 to 1)
    }

    @Test
    fun `should walk a straight line when no walkable route exists`() {
        // Given
        val nowhere: (Int, Int) -> Boolean = { _, _ -> false }
        // When
        val path = WalkPath.find(from = 0 to 0, to = 1 to 2, isWalkable = nowhere)
        // Then
        assertThat(path).containsExactly(0 to 1, 0 to 2, 1 to 2)
    }

    @Test
    fun `should not walk when the unit did not change tile`() {
        // Given
        val tile = 2 to 2
        // When
        val path = WalkPath.find(tile, tile, everywhere)
        // Then
        assertThat(path).isEmpty()
    }

    @Test
    fun `should jump straight to the destination when the walk is longer than the hop limit`() {
        // Given
        val to = 0 to WalkPath.MAX_HOPS + 3
        // When
        val path = WalkPath.find(from = 0 to 0, to = to, isWalkable = everywhere)
        // Then
        assertThat(path).containsExactly(to)
    }
}
