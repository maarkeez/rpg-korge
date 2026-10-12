package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleHudView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.rpg.screen.adapters.presentation.clickAbility
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.isDisplayingAttackPreview
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.rpg.screen.adapters.presentation.playerCallToActionView
import com.mkz.rpg.startGame
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import korlibs.korge.tests.ViewsForTesting
import org.junit.jupiter.api.Test

class CastAbilityEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup deploys a human player knight at (6,6), which can walk 3 tiles. (6,5) is an adjacent sand tile.
    private val knightRow = 6
    private val knightColumn = 6
    private val tileInMovementRangeRow = 6
    private val tileInMovementRangeColumn = 5

    // (6,4) is only reachable within 3 steps through (6,5): the paths around it take 4 steps.
    private val tileBehindAdjacentTileColumn = 4

    // The knight abilities are poisoned-sword, mushroom, skull, teleport, bee and heal, which is cast on the knight.
    // Bee deploys a bee on a vacant tile adjacent to the knight.
    private val beeAbilityIndex = 4
    private val healAbilityIndex = 5

    @Test
    fun `should keep the knight selected when the human player casts heal on the knight`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && canHumanPlayerAct() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            clickAbility(requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }, healAbilityIndex)
            waitUntil { battlefieldView.isDimmedAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            clickTile(battlefieldView, knightRow, knightColumn)
            val playerCallToActionView = requireNotNull(playerCallToActionView()) { "The call to action is not displayed" }
            waitUntil { playerCallToActionView.isDisplayingCancelAndConfirm() }
            // When
            clickConfirm(playerCallToActionView)
            // Then
            val battleHudView = requireNotNull(battleHudView()) { "The battle HUD is not displayed" }
            waitUntil { battleHudView.isDisplayingBattleUnitInfo() && !battlefieldView.isDimmedAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            withClue("Expected the knight info to be displayed after casting heal") { battleHudView.isDisplayingBattleUnitInfo().shouldBeTrue() }
            battleHudView.isDisplayingAttackPreview().shouldBeFalse()
            withClue("Expected the knight tile to be selected after casting heal") { battlefieldView.hasSelectionAt(knightRow, knightColumn).shouldBeTrue() }
            withClue("Expected the knight movement range to be displayed after casting heal") { battlefieldView.isDimmedAt(tileInMovementRangeRow, tileInMovementRangeColumn).shouldBeFalse() }
            battlefieldView.isDimmedAt(0, 0).shouldBeTrue()
        }

    @Test
    fun `should update the knight movement range when the human player deploys a bee on an adjacent tile`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && canHumanPlayerAct() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            require(!battlefieldView.isDimmedAt(tileInMovementRangeRow, tileBehindAdjacentTileColumn)) { "(6,4) should be reachable before the bee is deployed" }
            clickAbility(requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }, beeAbilityIndex)
            waitUntil { battlefieldView.isDimmedAt(tileInMovementRangeRow, tileBehindAdjacentTileColumn) }
            clickTile(battlefieldView, tileInMovementRangeRow, tileInMovementRangeColumn)
            val playerCallToActionView = requireNotNull(playerCallToActionView()) { "The call to action is not displayed" }
            waitUntil { playerCallToActionView.isDisplayingCancelAndConfirm() }
            // When
            clickConfirm(playerCallToActionView)
            // Then
            val battleHudView = requireNotNull(battleHudView()) { "The battle HUD is not displayed" }
            waitUntil { battleHudView.isDisplayingBattleUnitInfo() && battlefieldView.hasBattleUnitAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            waitUntil { !battlefieldView.isDimmedAt(knightRow - 1, knightColumn) }
            withClue("Expected the bee to be deployed at ($tileInMovementRangeRow,$tileInMovementRangeColumn)") {
                battlefieldView.hasBattleUnitAt(tileInMovementRangeRow, tileInMovementRangeColumn).shouldBeTrue()
            }
            withClue("Expected the knight movement range to be displayed after deploying the bee") { battlefieldView.isDimmedAt(knightRow - 1, knightColumn).shouldBeFalse() }
            withClue(
                "Expected (6,4) to be out of the knight movement range once the bee blocks the way",
            ) { battlefieldView.isDimmedAt(tileInMovementRangeRow, tileBehindAdjacentTileColumn).shouldBeTrue() }
        }
}
