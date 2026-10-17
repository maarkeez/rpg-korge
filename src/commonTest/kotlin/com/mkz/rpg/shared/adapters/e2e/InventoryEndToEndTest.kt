package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleHudView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickBag
import com.mkz.rpg.screen.adapters.presentation.clickClose
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.inventoryView
import com.mkz.rpg.screen.adapters.presentation.isBagDisplayed
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.slotBitmaps
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class InventoryEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The party walks to the battle with the leading knight at (4,3). The battle setup deploys a cpu rat at (0,0).
    // Nothing has been looted yet.
    private val knightRow = LEADER_ROW_IN_BATTLE
    private val knightColumn = LEADER_COLUMN_IN_BATTLE

    @Test
    fun `should open the empty inventory of the knight and go back to its info when the human player opens and closes its bag`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battleUnitInfoView()?.isBagDisplayed() == true }
            // When
            clickBag(requireNotNull(battleUnitInfoView()))
            waitUntil { inventoryView() != null }
            // Then
            val inventoryView = inventoryView().shouldNotBeNull()
            withClue("Expected the 12 slots of the knight inventory to be empty") { inventoryView.slotBitmaps() shouldBe List(12) { null } }
            clickClose(inventoryView)
            waitUntil { battleHudView()?.isDisplayingBattleUnitInfo() == true }
            withClue("Expected the knight info to be displayed again once the inventory is closed") { battleHudView()?.isDisplayingBattleUnitInfo()?.shouldBeTrue() }
        }

    @Test
    fun `should not display the bag when the human player selects a cpu rat`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickTile(battlefieldView, 0, 0)
            // Then
            waitUntil { battleHudView()?.isDisplayingBattleUnitInfo() == true }
            withClue("Expected the rat, which has no inventory, to have no bag") { battleUnitInfoView()?.isBagDisplayed()?.shouldBeFalse() }
        }
}
