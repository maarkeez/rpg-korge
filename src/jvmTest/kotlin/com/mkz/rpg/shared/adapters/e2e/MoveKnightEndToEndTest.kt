package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battleUnitPositions
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.maximumSteps
import com.mkz.rpg.screen.adapters.presentation.remainingSteps
import com.mkz.rpg.startGame
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
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
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && canHumanPlayerAct() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(targetRow, targetColumn) }
            // When
            clickTile(battlefieldView, targetRow, targetColumn)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, targetColumn) }
            withClue("Expected the knight to be displayed at ($targetRow,$targetColumn)") { battlefieldView.hasBattleUnitAt(targetRow, targetColumn).shouldBeTrue() }
            withClue("Expected the knight to be gone from ($knightRow,$knightColumn)") { battlefieldView.hasBattleUnitAt(knightRow, knightColumn).shouldBeFalse() }
        }

    @Test
    fun `should dim the tiles out of the knight movement range when the human player selects the knight`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && canHumanPlayerAct() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickTile(battlefieldView, knightRow, knightColumn)
            // Then
            waitUntil { battlefieldView.hasSelectionAt(targetRow, targetColumn) }
            withClue("Expected (0,0), out of the knight movement range, to be dimmed") { battlefieldView.isDimmedAt(0, 0).shouldBeTrue() }
            withClue("Expected the selected knight not to be dimmed") { battlefieldView.isDimmedAt(knightRow, knightColumn).shouldBeFalse() }
            withClue("Expected ($targetRow,$targetColumn), in the knight movement range, not to be dimmed") { battlefieldView.isDimmedAt(targetRow, targetColumn).shouldBeFalse() }
        }

    @Test
    fun `should walk the knight tile by tile when the human player moves it two tiles away`() =
        viewsTest {
            // Given
            val farTargetColumn = 4
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && canHumanPlayerAct() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(targetRow, farTargetColumn) }
            // When
            clickTile(battlefieldView, targetRow, farTargetColumn)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, targetColumn) }
            withClue("Expected the knight to be displayed only on the tile it walks through ($targetRow,$targetColumn)") {
                battlefieldView.battleUnitPositions().shouldContain(targetRow to targetColumn)
                battlefieldView.battleUnitPositions().shouldNotContainAnyOf(knightRow to knightColumn, targetRow to farTargetColumn)
            }
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, farTargetColumn) }
            withClue("Expected the knight to be displayed at ($targetRow,$farTargetColumn)") { battlefieldView.hasBattleUnitAt(targetRow, farTargetColumn).shouldBeTrue() }
            withClue("Expected the knight to be gone from ($targetRow,$targetColumn)") { battlefieldView.hasBattleUnitAt(targetRow, targetColumn).shouldBeFalse() }
            val battleUnitInfoView = requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }
            waitUntil { battleUnitInfoView.remainingSteps() == 1 }
            withClue("Expected 1 of the 3 knight steps to remain after walking 2 tiles") { battleUnitInfoView.remainingSteps() shouldBe 1 }
            battleUnitInfoView.maximumSteps() shouldBe 3
        }
}
