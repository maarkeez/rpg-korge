package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.callToActionView
import com.mkz.rpg.screen.adapters.presentation.clickA
import com.mkz.rpg.screen.adapters.presentation.clickClose
import com.mkz.rpg.screen.adapters.presentation.clickConfirmation
import com.mkz.rpg.screen.adapters.presentation.clickDialog
import com.mkz.rpg.screen.adapters.presentation.clickGiveGold
import com.mkz.rpg.screen.adapters.presentation.clickGoldButton
import com.mkz.rpg.screen.adapters.presentation.clickInventoryButton
import com.mkz.rpg.screen.adapters.presentation.clickLoadGame
import com.mkz.rpg.screen.adapters.presentation.clickNextOwner
import com.mkz.rpg.screen.adapters.presentation.clickPlus
import com.mkz.rpg.screen.adapters.presentation.clickQuit
import com.mkz.rpg.screen.adapters.presentation.clickSave
import com.mkz.rpg.screen.adapters.presentation.clickSlot
import com.mkz.rpg.screen.adapters.presentation.clickStart
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.confirmDialogView
import com.mkz.rpg.screen.adapters.presentation.giveGoldLabel
import com.mkz.rpg.screen.adapters.presentation.giveGoldView
import com.mkz.rpg.screen.adapters.presentation.goldButtonText
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.hudView
import com.mkz.rpg.screen.adapters.presentation.inventoryView
import com.mkz.rpg.screen.adapters.presentation.isDialogShown
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.isDisplayingGiveGold
import com.mkz.rpg.screen.adapters.presentation.isDisplayingInventory
import com.mkz.rpg.screen.adapters.presentation.isInventoryButtonDisplayed
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.isSavedShown
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.pauseView
import com.mkz.rpg.screen.adapters.presentation.saveSlotsView
import com.mkz.rpg.screen.adapters.presentation.slotBitmaps
import com.mkz.rpg.screen.adapters.presentation.talkView
import com.mkz.rpg.screen.adapters.presentation.titleText
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.isShown
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

private const val HERBALIST_ROW = 2
private const val HERBALIST_COLUMN = 2

class InventoryEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The party walks to the battle with the knight at (2,7), and the cpu bat engaging it walks to (2,8), next to it.
    // Nothing has been looted yet. The knight is selected once the human player turn starts.

    @Test
    fun `should open the empty inventory of the knight and go back to its info when the human player opens and closes its inventory`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            waitUntil { battleUnitInfoView()?.isInventoryButtonDisplayed() == true }
            // When
            clickInventoryButton(requireNotNull(battleUnitInfoView()))
            waitUntil { inventoryView() != null }
            // Then
            val inventoryView = inventoryView().shouldNotBeNull()
            withClue("Expected the 12 slots of the knight inventory to be empty") { inventoryView.slotBitmaps() shouldBe List(12) { null } }
            clickClose(inventoryView)
            waitUntil { hudView()?.isDisplayingBattleUnitInfo() == true }
            withClue("Expected the knight info to be displayed again once the inventory is closed") { hudView()?.isDisplayingBattleUnitInfo()?.shouldBeTrue() }
        }

    @Test
    fun `should not display the inventory button when the human player selects a cpu bat`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickTile(battlefieldView, BAT_ROW_IN_BATTLE, BAT_COLUMN_IN_BATTLE)
            // Then
            waitUntil { battlefieldView.hasSelectionAt(BAT_ROW_IN_BATTLE, BAT_COLUMN_IN_BATTLE) && hudView()?.isDisplayingBattleUnitInfo() == true }
            withClue("Expected the bat, which has no inventory, to have no inventory button") { battleUnitInfoView()?.isInventoryButtonDisplayed()?.shouldBeFalse() }
        }

    // The herbalist is a long way from where the party starts, so it is saved next to the hero, and joins once talked to.
    private suspend fun ViewsForTesting.startWithTheHerbalistInTheParty() {
        val storage = InmemoryStorage()
        startNewGame(storage = storage)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
        clickStart(requireNotNull(stage.virtualPadView()))
        waitUntil { stage.pauseView()?.isShown() == true }
        clickSave(requireNotNull(stage.pauseView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.pauseView()?.isSavedShown() == true }
        clickQuit(requireNotNull(stage.pauseView()))
        waitUntil { stage.confirmDialogView()?.isShown() == true }
        clickConfirmation(requireNotNull(stage.callToActionView()))
        waitUntil { stage.menuView()?.isShown() == true }
        val save = requireNotNull(storage.getOrNull("save.slot1")) { "Nothing is saved in the slot" }
        save shouldContain """"id":"herbalist","row":16,"column":15"""
        save shouldContain """"occupantId":"herbalist","row":16,"column":15"""
        storage["save.slot1"] =
            save
                .replace(""""id":"herbalist","row":16,"column":15""", """"id":"herbalist","row":$HERBALIST_ROW,"column":$HERBALIST_COLUMN""")
                .replace(""""occupantId":"herbalist","row":16,"column":15""", """"occupantId":"herbalist","row":$HERBALIST_ROW,"column":$HERBALIST_COLUMN""")
        clickLoadGame(requireNotNull(stage.menuView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.virtualPadView()?.isPadShown() == true }
        clickA(requireNotNull(stage.virtualPadView()))
        val talkView = requireNotNull(stage.talkView())
        waitUntil { talkView.isDialogShown() }
        clickDialog(talkView)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(HERBALIST_ROW, HERBALIST_COLUMN) == true }
        // The screen flashes it as it joins, and tapping a tile meanwhile is ignored.
        waitFor(1.seconds)
    }

    @Test
    fun `should give gold to another party member from the inventory, and read it in its inventory`() =
        sharedViewsTest {
            // Given: the hero carries 30 G, and the herbalist 10 G.
            startWithTheHerbalistInTheParty()
            clickTile(requireNotNull(battlefieldView()), KNIGHT_START_ROW, KNIGHT_START_COLUMN)
            waitUntil { battleUnitInfoView()?.isInventoryButtonDisplayed() == true }
            clickInventoryButton(requireNotNull(battleUnitInfoView()))
            waitUntil { hudView()?.isDisplayingInventory() == true }
            val inventoryView = inventoryView().shouldNotBeNull()
            inventoryView.goldButtonText() shouldBe "30 G"
            // When
            clickGoldButton(inventoryView)
            waitUntil { hudView()?.isDisplayingGiveGold() == true }
            clickPlus(giveGoldView().shouldNotBeNull())
            waitUntil { callToActionView()?.giveGoldLabel() == "Give 2 G" }
            callToActionView()?.giveGoldLabel() shouldBe "Give 2 G"
            clickGiveGold(callToActionView().shouldNotBeNull())
            // Then
            waitUntil { hudView()?.isDisplayingInventory() == true }
            withClue("Expected the inventory of the hero to be displayed again") { inventoryView.titleText() shouldBe "Mage" }
            withClue("Expected the inventory to be displayed again with the gold left") { inventoryView.goldButtonText() shouldBe "28 G" }
            clickNextOwner(inventoryView)
            waitUntil { inventoryView.titleText() == "Herbalist" }
            withClue("Expected the herbalist to carry the gold") { inventoryView.goldButtonText() shouldBe "12 G" }
        }
}
