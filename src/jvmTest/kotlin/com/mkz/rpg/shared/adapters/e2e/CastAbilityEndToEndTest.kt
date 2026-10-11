package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleHudView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickAbility
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.isDisplayingAttackPreview
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.rpg.screen.adapters.presentation.isTurnAnnounced
import com.mkz.rpg.screen.adapters.presentation.playerCallToActionView
import com.mkz.rpg.startGame
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CastAbilityEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup deploys a human player knight at (6,6), which can walk 3 tiles. (6,5) is an adjacent sand tile.
    private val knightRow = 6
    private val knightColumn = 6
    private val tileInMovementRangeRow = 6
    private val tileInMovementRangeColumn = 5

    // The knight abilities are poisoned-sword, mushroom, skull, teleport, bee and heal, which is cast on the knight.
    private val healAbilityIndex = 5

    @Test
    fun `should keep the knight selected when the human player casts heal on the knight`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && isTurnAnnounced() }
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
            assertThat(battleHudView.isDisplayingBattleUnitInfo())
                .withFailMessage("Expected the knight info to be displayed after casting heal")
                .isTrue
            assertThat(battleHudView.isDisplayingAttackPreview()).isFalse
            assertThat(battlefieldView.hasSelectionAt(knightRow, knightColumn))
                .withFailMessage("Expected the knight tile to be selected after casting heal")
                .isTrue
            assertThat(battlefieldView.isDimmedAt(tileInMovementRangeRow, tileInMovementRangeColumn))
                .withFailMessage("Expected the knight movement range to be displayed after casting heal")
                .isFalse
            assertThat(battlefieldView.isDimmedAt(0, 0)).isTrue
        }
}
