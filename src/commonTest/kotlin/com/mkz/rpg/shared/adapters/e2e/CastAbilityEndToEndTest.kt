package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleHudView
import com.mkz.rpg.screen.adapters.presentation.battleInfoView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickAbility
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isBattleHudShown
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.isDisplayingAttackPreview
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.playerCallToActionView
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class CastAbilityEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The party walks to the battle with the knight at (2,5), which can walk 3 tiles. (2,4) is an adjacent sand tile.
    private val knightRow = LEADER_ROW_IN_BATTLE
    private val knightColumn = LEADER_COLUMN_IN_BATTLE
    private val tileInMovementRangeRow = LEADER_ROW_IN_BATTLE
    private val tileInMovementRangeColumn = 4

    // (2,1) is 4 tiles away along the corridor, out of the knight movement range.
    private val tileOutOfRangeColumn = 1

    // The bat engaging the knight, alone, stands next to it. The bat has 10 health points, and the knight sword deals 10.
    private val batRow = BAT_ROW_IN_BATTLE
    private val batColumn = BAT_COLUMN_IN_BATTLE

    // The knight abilities are sword, which is cast on an adjacent enemy, and heal, which is cast on the knight.
    private val swordAbilityIndex = 0
    private val healAbilityIndex = 1

    @Test
    fun `should keep the knight selected when the human player casts heal on the knight`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
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
            battlefieldView.isDimmedAt(tileInMovementRangeRow, tileOutOfRangeColumn).shouldBeTrue()
        }

    @Test
    fun `should explore again when the human player defeats the bat, the only cpu battle unit engaged, with the sword`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            waitUntil { battlefieldView.hasSelectionAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            clickAbility(requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }, swordAbilityIndex)
            waitUntil { battlefieldView.isDimmedAt(tileInMovementRangeRow, tileInMovementRangeColumn) }
            clickTile(battlefieldView, batRow, batColumn)
            val playerCallToActionView = requireNotNull(playerCallToActionView()) { "The call to action is not displayed" }
            waitUntil { playerCallToActionView.isDisplayingCancelAndConfirm() }
            // When
            clickConfirm(playerCallToActionView)
            // Then: the battle finishes straight away, without finishing the turn.
            waitUntil { virtualPadView()?.isPadShown() == true && battleInfoView()?.roundText() == "Exploring" }
            withClue("Expected the bat to be defeated and gone from ($batRow,$batColumn)") { battlefieldView.hasBattleUnitAt(batRow, batColumn).shouldBeFalse() }
            withClue("Expected the virtual pad to be displayed once the battle is over") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the battle HUD to be hidden once the battle is over") { isBattleHudShown().shouldBeFalse() }
            battleInfoView()?.roundText() shouldBe "Exploring"
        }
}
