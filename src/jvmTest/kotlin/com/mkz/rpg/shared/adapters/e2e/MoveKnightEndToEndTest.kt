package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleUnitPositions
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isTurnAnnounced
import com.mkz.rpg.startGame
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MoveKnightEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup deploys a human player knight at (6,6), which can walk 3 tiles. (6,5) and (6,4) are sand tiles.
    private val knightRow = 6
    private val knightColumn = 6
    private val targetRow = 6
    private val targetColumn = 5

    @Test
    fun `should move the knight one tile when the human player selects the knight and an adjacent tile`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && isTurnAnnounced() }
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

    @Test
    fun `should walk the knight tile by tile when the human player moves it two tiles away`() =
        viewsTest {
            // Given
            val farTargetColumn = 4
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && isTurnAnnounced() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(targetRow, farTargetColumn) }
            // When
            clickTile(battlefieldView, targetRow, farTargetColumn)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, targetColumn) }
            assertThat(battlefieldView.battleUnitPositions())
                .withFailMessage("Expected the knight to be displayed only on the tile it walks through (%s,%s)", targetRow, targetColumn)
                .contains(targetRow to targetColumn)
                .doesNotContain(knightRow to knightColumn, targetRow to farTargetColumn)
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, farTargetColumn) }
            assertThat(battlefieldView.hasBattleUnitAt(targetRow, farTargetColumn))
                .withFailMessage("Expected the knight to be displayed at (%s,%s)", targetRow, farTargetColumn)
                .isTrue
            assertThat(battlefieldView.hasBattleUnitAt(targetRow, targetColumn))
                .withFailMessage("Expected the knight to be gone from (%s,%s)", targetRow, targetColumn)
                .isFalse
        }
}
