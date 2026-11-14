package com.mkz.tessavar.shared.adapters.e2e

import com.mkz.tessavar.GAME_WINDOW_SIZE
import com.mkz.tessavar.screen.adapters.presentation.BattlefieldView
import com.mkz.tessavar.screen.adapters.presentation.battleUnitInfoView
import com.mkz.tessavar.screen.adapters.presentation.battleUnitPositions
import com.mkz.tessavar.screen.adapters.presentation.battlefieldView
import com.mkz.tessavar.screen.adapters.presentation.clickTile
import com.mkz.tessavar.screen.adapters.presentation.glidingBattleUnits
import com.mkz.tessavar.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.tessavar.screen.adapters.presentation.hasSelectionAt
import com.mkz.tessavar.screen.adapters.presentation.isDimmedAt
import com.mkz.tessavar.screen.adapters.presentation.maximumSteps
import com.mkz.tessavar.screen.adapters.presentation.remainingSteps
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MoveKnightEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private val knightRow = LEADER_ROW_IN_BATTLE
    private val knightColumn = LEADER_COLUMN_IN_BATTLE
    private val targetRow = LEADER_ROW_IN_BATTLE
    private val targetColumn = LEADER_COLUMN_IN_BATTLE - 1
    private val outOfRangeColumn = LEADER_COLUMN_IN_BATTLE - 4

    @Test
    fun `should move the knight one tile when the human player selects the knight and an adjacent tile`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            waitUntil { battlefieldView.hasSelectionAt(targetRow, targetColumn) }
            // When
            clickTile(battlefieldView, targetRow, targetColumn)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, targetColumn) }
            withClue("Expected the knight to be displayed at ($targetRow,$targetColumn)") { battlefieldView.hasBattleUnitAt(targetRow, targetColumn).shouldBeTrue() }
            withClue("Expected the knight to be gone from ($knightRow,$knightColumn)") { battlefieldView.hasBattleUnitAt(knightRow, knightColumn).shouldBeFalse() }
        }

    @Test
    fun `should select the knight and dim the tiles out of its walking range when the human player turn starts`() =
        sharedViewsTest {
            // Given
            // When
            startNewBattle()
            // Then
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            waitUntil { battlefieldView.hasSelectionAt(targetRow, targetColumn) }
            withClue("Expected ($targetRow,$outOfRangeColumn), out of the knight walking range, to be dimmed") { battlefieldView.isDimmedAt(targetRow, outOfRangeColumn).shouldBeTrue() }
            withClue("Expected the selected knight not to be dimmed") { battlefieldView.isDimmedAt(knightRow, knightColumn).shouldBeFalse() }
            withClue("Expected ($targetRow,$targetColumn), in the knight walking range, not to be dimmed") { battlefieldView.isDimmedAt(targetRow, targetColumn).shouldBeFalse() }
        }

    @Test
    fun `should glide the knight tile by tile when the human player moves it two tiles away`() =
        sharedViewsTest {
            // Given
            val farTargetColumn = LEADER_COLUMN_IN_BATTLE - 2
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            waitUntil { battlefieldView.hasSelectionAt(targetRow, farTargetColumn) }
            // When
            clickTile(battlefieldView, targetRow, farTargetColumn)
            // Then
            val throughTileX = targetColumn * BattlefieldView.TILE_SIZE.toDouble()
            waitUntil { battlefieldView.glidingBattleUnits().any { glidingUnit -> glidingUnit.x < throughTileX } }
            withClue("Expected the knight to glide past the tile it walks through ($targetRow,$targetColumn), off the tiles") {
                battlefieldView.glidingBattleUnits().any { glidingUnit -> glidingUnit.x < throughTileX }.shouldBeTrue()
                battlefieldView.battleUnitPositions().shouldNotContainAnyOf(knightRow to knightColumn, targetRow to targetColumn, targetRow to farTargetColumn)
            }
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, farTargetColumn) }
            withClue("Expected the knight to be displayed at ($targetRow,$farTargetColumn)") { battlefieldView.hasBattleUnitAt(targetRow, farTargetColumn).shouldBeTrue() }
            withClue("Expected the knight to be gone from ($targetRow,$targetColumn)") { battlefieldView.hasBattleUnitAt(targetRow, targetColumn).shouldBeFalse() }
            withClue("Expected the knight to have stopped gliding") { battlefieldView.glidingBattleUnits().shouldBeEmpty() }
            val battleUnitInfoView = requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }
            waitUntil { battleUnitInfoView.remainingSteps() == 1 }
            withClue("Expected 1 of the 3 knight steps to remain after walking 2 tiles") { battleUnitInfoView.remainingSteps() shouldBe 1 }
            battleUnitInfoView.maximumSteps() shouldBe 3
        }
}
