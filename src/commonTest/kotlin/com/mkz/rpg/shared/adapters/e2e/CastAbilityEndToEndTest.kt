package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.bannerView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.callToActionView
import com.mkz.rpg.screen.adapters.presentation.clickAbility
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.hudView
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCastPreview
import com.mkz.rpg.screen.adapters.presentation.isHudShown
import com.mkz.rpg.screen.adapters.presentation.isPadShown
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
    // The party walks to the battle with the knight at (2,7), which can walk 3 tiles. (2,6) is an adjacent sand tile.
    private val knightRow = LEADER_ROW_IN_BATTLE
    private val knightColumn = LEADER_COLUMN_IN_BATTLE
    private val tileInWalkingRangeRow = LEADER_ROW_IN_BATTLE
    private val tileInWalkingRangeColumn = LEADER_COLUMN_IN_BATTLE - 1

    // (2,3) is 4 tiles away across the start room, out of the knight walking range.
    private val tileOutOfRangeColumn = LEADER_COLUMN_IN_BATTLE - 4

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
            waitUntil { battlefieldView.hasSelectionAt(tileInWalkingRangeRow, tileInWalkingRangeColumn) }
            clickAbility(requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }, healAbilityIndex)
            waitUntil { battlefieldView.isDimmedAt(tileInWalkingRangeRow, tileInWalkingRangeColumn) }
            clickTile(battlefieldView, knightRow, knightColumn)
            val callToActionView = requireNotNull(callToActionView()) { "The call to action is not displayed" }
            waitUntil { callToActionView.isDisplayingCancelAndConfirm() }
            // When
            clickConfirm(callToActionView)
            // Then
            val hudView = requireNotNull(hudView()) { "The HUD is not displayed" }
            waitUntil { hudView.isDisplayingBattleUnitInfo() && !battlefieldView.isDimmedAt(tileInWalkingRangeRow, tileInWalkingRangeColumn) }
            withClue("Expected the knight info to be displayed after casting heal") { hudView.isDisplayingBattleUnitInfo().shouldBeTrue() }
            hudView.isDisplayingCastPreview().shouldBeFalse()
            withClue("Expected the knight tile to be selected after casting heal") { battlefieldView.hasSelectionAt(knightRow, knightColumn).shouldBeTrue() }
            withClue("Expected the knight walking range to be displayed after casting heal") { battlefieldView.isDimmedAt(tileInWalkingRangeRow, tileInWalkingRangeColumn).shouldBeFalse() }
            battlefieldView.isDimmedAt(tileInWalkingRangeRow, tileOutOfRangeColumn).shouldBeTrue()
        }

    @Test
    fun `should explore again when the human player defeats the bat, the only cpu battle unit in the battle, with the sword`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            waitUntil { battlefieldView.hasSelectionAt(tileInWalkingRangeRow, tileInWalkingRangeColumn) }
            clickAbility(requireNotNull(battleUnitInfoView()) { "The battle unit info is not displayed" }, swordAbilityIndex)
            waitUntil { battlefieldView.isDimmedAt(tileInWalkingRangeRow, tileInWalkingRangeColumn) }
            clickTile(battlefieldView, batRow, batColumn)
            val callToActionView = requireNotNull(callToActionView()) { "The call to action is not displayed" }
            waitUntil { callToActionView.isDisplayingCancelAndConfirm() }
            // When
            clickConfirm(callToActionView)
            // Then: the battle finishes straight away, without finishing the turn.
            waitUntil { virtualPadView()?.isPadShown() == true && bannerView()?.roundText() == "Exploring" }
            withClue("Expected the bat to be defeated and gone from ($batRow,$batColumn)") { battlefieldView.hasBattleUnitAt(batRow, batColumn).shouldBeFalse() }
            withClue("Expected the virtual pad to be displayed once the battle is over") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the HUD to be hidden once the battle is over") { isHudShown().shouldBeFalse() }
            bannerView()?.roundText() shouldBe "Exploring"
        }
}
