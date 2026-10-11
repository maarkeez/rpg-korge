package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.startGame
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.yield
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MoveKnightEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup deploys a human player knight at (6,6). (6,5) is an adjacent sand tile.
    private val knightRow = 6
    private val knightColumn = 6
    private val targetRow = 6
    private val targetColumn = 5

    @Test
    fun `should move the knight one tile when the human player selects the knight and an adjacent tile`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(targetRow, targetColumn) }
            // When
            clickTile(battlefieldView, targetRow, targetColumn)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, targetColumn) }
            assertThat(battlefieldView.hasBattleUnitAt(targetRow, targetColumn))
                .withFailMessage("Expected the knight to be displayed at (%s,%s)", targetRow, targetColumn)
                .isTrue
            assertThat(battlefieldView.hasBattleUnitAt(knightRow, knightColumn))
                .withFailMessage("Expected the knight to be gone from (%s,%s)", knightRow, knightColumn)
                .isFalse
        }

    // Frames are simulated much faster than real time, while the game loads its resources
    // asynchronously in real time. Hence, waiting is bounded by real time instead of frames,
    // and `yield()` lets the game coroutines run between frames.
    private suspend fun waitUntil(condition: () -> Boolean) {
        val startMillis = System.currentTimeMillis()
        while (!condition() && System.currentTimeMillis() - startMillis < WAIT_TIMEOUT_MILLIS) {
            delayFrame()
            yield()
        }
    }

    private companion object {
        const val WAIT_TIMEOUT_MILLIS = 10_000
    }
}
