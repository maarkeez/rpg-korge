package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.rpg.screen.adapters.presentation.battleInfoView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.rpg.screen.adapters.presentation.clickAbility
import com.mkz.rpg.screen.adapters.presentation.clickArrow
import com.mkz.rpg.screen.adapters.presentation.clickBack
import com.mkz.rpg.screen.adapters.presentation.clickCapsule
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickLoadGame
import com.mkz.rpg.screen.adapters.presentation.clickQuit
import com.mkz.rpg.screen.adapters.presentation.clickSave
import com.mkz.rpg.screen.adapters.presentation.clickSlot
import com.mkz.rpg.screen.adapters.presentation.clickStart
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.confirmDialogView
import com.mkz.rpg.screen.adapters.presentation.displayedCaption
import com.mkz.rpg.screen.adapters.presentation.displayedGold
import com.mkz.rpg.screen.adapters.presentation.displayedName
import com.mkz.rpg.screen.adapters.presentation.displayedTask
import com.mkz.rpg.screen.adapters.presentation.displayedTitle
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isAnnouncementShown
import com.mkz.rpg.screen.adapters.presentation.isChevronShown
import com.mkz.rpg.screen.adapters.presentation.isDetailShown
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.isSavedShown
import com.mkz.rpg.screen.adapters.presentation.isTickShown
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.pauseView
import com.mkz.rpg.screen.adapters.presentation.playerCallToActionView
import com.mkz.rpg.screen.adapters.presentation.questAnnouncementView
import com.mkz.rpg.screen.adapters.presentation.questDetailView
import com.mkz.rpg.screen.adapters.presentation.questTrackerView
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.screen.adapters.presentation.saveSlotsView
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.isShown
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private val swordAbilityIndex = 0
    private val saveSlotKey = "save.slot1"
    private val gateBat = """"id":"gate-bat","unitId":"bat""""
    private val gateBatAsTheGoatWizard = """"id":"gate-bat","unitId":"goat-wizard""""

    private val storage = InmemoryStorage()

    // A new game, once the scene shows the party.
    private suspend fun ViewsForTesting.startExploring() {
        startNewGame(storage = storage)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
    }

    // Saves the game in slot 1, and quits to the menu.
    private suspend fun ViewsForTesting.saveAndQuit() {
        clickStart(requireNotNull(stage.virtualPadView()))
        waitUntil { stage.pauseView()?.isShown() == true }
        clickSave(requireNotNull(stage.pauseView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.pauseView()?.isSavedShown() == true }
        clickQuit(requireNotNull(stage.pauseView()))
        waitUntil { stage.confirmDialogView()?.isShown() == true }
        clickConfirm(requireNotNull(stage.confirmDialogView()))
        waitUntil { stage.menuView()?.isShown() == true }
    }

    private suspend fun ViewsForTesting.loadSlot() {
        clickLoadGame(requireNotNull(stage.menuView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.questTrackerView()?.displayedTask() != null }
    }

    // The goat wizard is a whole game away, so the bat on the gate, the first CPU battle unit to engage the party, is saved
    // as the goat wizard. It keeps the stats of the bat, as a save holds them, so the sword of the knight defeats it.
    private fun makeTheGateBatTheGoatWizardInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain gateBat
        storage[saveSlotKey] = save.replace(gateBat, gateBatAsTheGoatWizard)
    }

    @Test
    fun `should announce the main quest with a toast and let the player walk when a new game starts`() =
        sharedViewsTest {
            // When
            startExploring()
            // Then
            val announcementView = requireNotNull(questAnnouncementView())
            withClue("Expected the toast to announce the quest") { announcementView.isAnnouncementShown().shouldBeTrue() }
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "The Goat Wizard's Lair"
            announcementView.displayedTask() shouldBe "Defeat the Goat Wizard"
            requireNotNull(questTrackerView()).displayedTask() shouldBe "Defeat the Goat Wizard"
            // When
            clickArrow(requireNotNull(virtualPadView()), Direction.EAST)
            waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN + 1) == true }
            // Then
            withClue("Expected the party to walk under the toast") { announcementView.isAnnouncementShown().shouldBeTrue() }
            // When
            waitUntil { !announcementView.isAnnouncementShown() }
            // Then
            withClue("Expected the toast to fade by itself") { announcementView.isAnnouncementShown().shouldBeFalse() }
        }

    @Test
    fun `should open the detail view in place of the pad when the tracker is tapped, and display the pad again with Back`() =
        sharedViewsTest {
            // Given
            startExploring()
            // When
            clickCapsule(requireNotNull(questTrackerView()))
            waitUntil { questDetailView()?.isDetailShown() == true }
            // Then
            withClue("Expected the detail view to be displayed") { questDetailView()?.isDetailShown()?.shouldBeTrue() }
            withClue("Expected the virtual pad to be hidden while reading the quest") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
            questDetailView()?.displayedTask() shouldBe "Defeat the Goat Wizard"
            // When
            clickBack(requireNotNull(questDetailView()))
            waitUntil { virtualPadView()?.isPadShown() == true }
            // Then
            withClue("Expected the virtual pad to be displayed again") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the detail view to be hidden") { questDetailView()?.isDetailShown()?.shouldBeFalse() }
        }

    @Test
    fun `should announce the reward and tick the tracker when the goat wizard is defeated`() =
        sharedViewsTest {
            // Given: a game whose first CPU battle unit to engage the party is the goat wizard.
            startExploring()
            saveAndQuit()
            makeTheGateBatTheGoatWizardInTheSave()
            loadSlot()
            // And the party walks to the battle, where the tracker is read only.
            repeat(LEADER_COLUMN_IN_BATTLE - KNIGHT_START_COLUMN) { step ->
                clickArrow(requireNotNull(virtualPadView()), Direction.EAST)
                waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN + step + 1) == true }
            }
            waitUntil { canHumanPlayerAct() }
            val trackerView = requireNotNull(questTrackerView())
            withClue("Expected no chevron in a battle") { trackerView.isChevronShown().shouldBeFalse() }
            // When: the knight defeats it with the sword.
            val battlefieldView = requireNotNull(battlefieldView())
            val playerCallToActionView = requireNotNull(playerCallToActionView())
            waitUntil { battlefieldView.hasSelectionAt(LEADER_ROW_IN_BATTLE, LEADER_COLUMN_IN_BATTLE - 1) }
            clickAbility(requireNotNull(battleUnitInfoView()), swordAbilityIndex)
            waitUntil { battlefieldView.isDimmedAt(LEADER_ROW_IN_BATTLE, LEADER_COLUMN_IN_BATTLE - 1) }
            clickTile(battlefieldView, BAT_ROW_IN_BATTLE, BAT_COLUMN_IN_BATTLE)
            waitUntil { playerCallToActionView.isDisplayingCancelAndConfirm() }
            clickConfirm(playerCallToActionView)
            val announcementView = requireNotNull(questAnnouncementView())
            waitUntil { announcementView.isAnnouncementShown() }
            // Then
            withClue("Expected the toast to announce the completion") { announcementView.isAnnouncementShown().shouldBeTrue() }
            announcementView.displayedTitle() shouldBe "QUEST COMPLETED"
            announcementView.displayedGold().shouldContainExactly("150 G")
            trackerView.displayedTask() shouldBe "Quest completed"
            withClue("Expected a ticked tracker") { trackerView.isTickShown().shouldBeTrue() }
            // When: the battle ends.
            waitUntil { virtualPadView()?.isPadShown() == true && battleInfoView()?.roundText() == "Exploring" }
            // Then: the ticked tracker opens the completed detail.
            withClue("Expected the chevron back once exploring") { trackerView.isChevronShown().shouldBeTrue() }
            clickCapsule(trackerView)
            waitUntil { questDetailView()?.isDetailShown() == true }
            questDetailView()?.displayedCaption() shouldBe "QUEST COMPLETED"
        }

    @Test
    fun `should not announce and should display the tracker when a game is loaded`() =
        sharedViewsTest {
            // Given
            startExploring()
            saveAndQuit()
            // When
            loadSlot()
            // Then
            requireNotNull(questTrackerView()).displayedTask() shouldBe "Defeat the Goat Wizard"
            withClue("Expected no toast in a loaded game") { questAnnouncementView()?.isAnnouncementShown()?.shouldBeFalse() }
        }
}
