package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.HudStyle
import com.mkz.rpg.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.rpg.screen.adapters.presentation.bannerView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.callToActionView
import com.mkz.rpg.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.rpg.screen.adapters.presentation.clickA
import com.mkz.rpg.screen.adapters.presentation.clickAbility
import com.mkz.rpg.screen.adapters.presentation.clickAction
import com.mkz.rpg.screen.adapters.presentation.clickArrow
import com.mkz.rpg.screen.adapters.presentation.clickCancelConfirmation
import com.mkz.rpg.screen.adapters.presentation.clickCapsule
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickConfirmation
import com.mkz.rpg.screen.adapters.presentation.clickDialog
import com.mkz.rpg.screen.adapters.presentation.clickLoadGame
import com.mkz.rpg.screen.adapters.presentation.clickPin
import com.mkz.rpg.screen.adapters.presentation.clickQuit
import com.mkz.rpg.screen.adapters.presentation.clickRow
import com.mkz.rpg.screen.adapters.presentation.clickSave
import com.mkz.rpg.screen.adapters.presentation.clickSlot
import com.mkz.rpg.screen.adapters.presentation.clickStart
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.confirmDialogView
import com.mkz.rpg.screen.adapters.presentation.displayedCaption
import com.mkz.rpg.screen.adapters.presentation.displayedCount
import com.mkz.rpg.screen.adapters.presentation.displayedGold
import com.mkz.rpg.screen.adapters.presentation.displayedName
import com.mkz.rpg.screen.adapters.presentation.displayedRows
import com.mkz.rpg.screen.adapters.presentation.displayedTask
import com.mkz.rpg.screen.adapters.presentation.displayedTitle
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.hasQuestMarkerAt
import com.mkz.rpg.screen.adapters.presentation.hasSelectionAt
import com.mkz.rpg.screen.adapters.presentation.isAnnouncementShown
import com.mkz.rpg.screen.adapters.presentation.isChevronShown
import com.mkz.rpg.screen.adapters.presentation.isDetailShown
import com.mkz.rpg.screen.adapters.presentation.isDialogShown
import com.mkz.rpg.screen.adapters.presentation.isDimmedAt
import com.mkz.rpg.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.isPinShown
import com.mkz.rpg.screen.adapters.presentation.isPinnedRow
import com.mkz.rpg.screen.adapters.presentation.isSavedShown
import com.mkz.rpg.screen.adapters.presentation.isTickShown
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.pauseView
import com.mkz.rpg.screen.adapters.presentation.questAnnouncementView
import com.mkz.rpg.screen.adapters.presentation.questDetailView
import com.mkz.rpg.screen.adapters.presentation.questJournalView
import com.mkz.rpg.screen.adapters.presentation.questMarkerColorAt
import com.mkz.rpg.screen.adapters.presentation.questTrackerView
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.screen.adapters.presentation.saveSlotsView
import com.mkz.rpg.screen.adapters.presentation.talkView
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

private const val FARMER_ROW = 1
private const val FARMER_COLUMN = 1
private const val ELDER_ROW = 16
private const val ELDER_COLUMN = 8

class QuestEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private val swordAbilityIndex = 0
    private val saveSlotKey = "save.slot1"
    private val gateBat = """"id":"gate-bat","unitId":"bat""""
    private val gateBatAsTheGoatWizard = """"id":"gate-bat","unitId":"goat-wizard""""
    private val farmerInTheSave = """"id":"farmer","row":8,"column":12"""
    private val farmerOnTheBattlefieldInTheSave = """"occupantId":"farmer","row":8,"column":12"""
    private val batsQuestStarted = """"questId":"bats-in-the-meadow","status":"ACTIVE","currentTaskIndex":0,"currentTaskDefeats":0"""
    private val batsQuest = "bats-in-the-meadow"
    private val goatWizardQuest = "defeat-the-goat-wizard"

    private val storage = InmemoryStorage()

    // A new game, once the scene shows the party.
    private suspend fun ViewsForTesting.startExploring() {
        startNewGame(storage = storage)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
    }

    // Saves the game in slot 1, overwriting it, and quits to the menu.
    private suspend fun ViewsForTesting.saveAndQuit() {
        clickStart(requireNotNull(stage.virtualPadView()))
        waitUntil { stage.pauseView()?.isShown() == true }
        clickSave(requireNotNull(stage.pauseView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        // A slot already holding a game asks before overwriting it.
        waitUntil { stage.pauseView()?.isSavedShown() == true || stage.confirmDialogView()?.isShown() == true }
        if (stage.confirmDialogView()?.isShown() == true) clickConfirmation(requireNotNull(stage.callToActionView()))
        waitUntil { stage.pauseView()?.isSavedShown() == true }
        clickQuit(requireNotNull(stage.pauseView()))
        waitUntil { stage.confirmDialogView()?.isShown() == true }
        clickConfirmation(requireNotNull(stage.callToActionView()))
        waitUntil { stage.menuView()?.isShown() == true }
    }

    private suspend fun ViewsForTesting.loadSlot() {
        clickLoadGame(requireNotNull(stage.menuView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.questTrackerView()?.displayedTask() != null }
    }

    // The farmer is a battle away from where the party starts, so it is saved next to the knight.
    private fun moveTheFarmerNextToTheKnightInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain farmerInTheSave
        save shouldContain farmerOnTheBattlefieldInTheSave
        storage[saveSlotKey] =
            save
                .replace(farmerInTheSave, """"id":"farmer","row":$FARMER_ROW,"column":$FARMER_COLUMN""")
                .replace(farmerOnTheBattlefieldInTheSave, """"occupantId":"farmer","row":$FARMER_ROW,"column":$FARMER_COLUMN""")
    }

    // Two bats are a long way off, so the save counts them as defeated, leaving the bat on the gate as the third.
    private fun countTwoBatsInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain batsQuestStarted
        storage[saveSlotKey] = save.replace(batsQuestStarted, batsQuestStarted.replace(""""currentTaskDefeats":0""", """"currentTaskDefeats":2"""))
    }

    // A new game with the farmer next to the knight.
    private suspend fun ViewsForTesting.startWithTheFarmerNextToTheKnight() {
        startExploring()
        saveAndQuit()
        moveTheFarmerNextToTheKnightInTheSave()
        loadSlot()
    }

    // Talks to the npc next to the leader and finishes the talk, waiting until the pad is back.
    private suspend fun ViewsForTesting.talkToTheNpcNextToTheLeader() {
        clickA(requireNotNull(stage.virtualPadView()))
        val talkView = requireNotNull(stage.talkView())
        waitUntil { talkView.isDialogShown() }
        clickDialog(talkView)
        waitUntil { stage.virtualPadView()?.isPadShown() == true }
    }

    // The party walks to the battle, where the bat on the gate engages it, and the knight defeats it with the sword.
    private suspend fun ViewsForTesting.defeatTheGateBat() {
        repeat(LEADER_COLUMN_IN_BATTLE - KNIGHT_START_COLUMN) { step ->
            clickArrow(requireNotNull(stage.virtualPadView()), Direction.EAST)
            waitUntil { stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN + step + 1) == true }
        }
        waitUntil { stage.canHumanPlayerAct() }
        val battlefieldView = requireNotNull(stage.battlefieldView())
        val callToActionView = requireNotNull(stage.callToActionView())
        waitUntil { battlefieldView.hasSelectionAt(LEADER_ROW_IN_BATTLE, LEADER_COLUMN_IN_BATTLE - 1) }
        clickAbility(requireNotNull(stage.battleUnitInfoView()), swordAbilityIndex)
        waitUntil { battlefieldView.isDimmedAt(LEADER_ROW_IN_BATTLE, LEADER_COLUMN_IN_BATTLE - 1) }
        clickTile(battlefieldView, BAT_ROW_IN_BATTLE, BAT_COLUMN_IN_BATTLE)
        waitUntil { callToActionView.isDisplayingCancelAndConfirm() }
        clickConfirm(callToActionView)
    }

    // The goat wizard is a whole game away, so the bat on the gate, the first CPU battle unit to engage the party, is saved
    // as the goat wizard. It keeps the stats of the bat, as a save holds them, so the sword of the knight defeats it.
    private fun makeTheGateBatTheGoatWizardInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain gateBat
        storage[saveSlotKey] = save.replace(gateBat, gateBatAsTheGoatWizard)
    }

    @Test
    fun `should announce the main quest with an announcement and let the player walk when a new game starts`() =
        sharedViewsTest {
            // When
            startExploring()
            // Then
            val announcementView = requireNotNull(questAnnouncementView())
            withClue("Expected the announcement to announce the quest") { announcementView.isAnnouncementShown().shouldBeTrue() }
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "The Goat Wizard's Lair"
            announcementView.displayedTask() shouldBe "Defeat the Goat Wizard"
            requireNotNull(questTrackerView()).displayedTask() shouldBe "Defeat the Goat Wizard"
            // When
            clickArrow(requireNotNull(virtualPadView()), Direction.EAST)
            waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN + 1) == true }
            // Then
            withClue("Expected the party to walk under the announcement") { announcementView.isAnnouncementShown().shouldBeTrue() }
            // When
            waitUntil { !announcementView.isAnnouncementShown() }
            // Then
            withClue("Expected the announcement to fade by itself") { announcementView.isAnnouncementShown().shouldBeFalse() }
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
            clickAction(requireNotNull(stage.callToActionView()), "Back")
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
            val trackerView = requireNotNull(questTrackerView())
            // When: the party walks to the battle, and the knight defeats it with the sword.
            defeatTheGateBat()
            val announcementView = requireNotNull(questAnnouncementView())
            waitUntil { announcementView.isAnnouncementShown() }
            // Then
            withClue("Expected the announcement to announce the completion") { announcementView.isAnnouncementShown().shouldBeTrue() }
            announcementView.displayedTitle() shouldBe "QUEST COMPLETED"
            announcementView.displayedGold().shouldContainExactly("150 G")
            trackerView.displayedTask() shouldBe "Quest completed"
            withClue("Expected a ticked tracker") { trackerView.isTickShown().shouldBeTrue() }
            // When: the battle ends.
            waitUntil { virtualPadView()?.isPadShown() == true && bannerView()?.roundText() == "Exploring" }
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
            withClue("Expected no announcement in a loaded game") { questAnnouncementView()?.isAnnouncementShown()?.shouldBeFalse() }
        }

    @Test
    fun `should play a side quest when it is started, pinned, progressed and completed`() =
        sharedViewsTest {
            // Given
            startWithTheFarmerNextToTheKnight()
            withClue("Expected the gold marker over the farmer") { battlefieldView()?.questMarkerColorAt(FARMER_ROW, FARMER_COLUMN) shouldBe HudStyle.SELECTION }
            // When: the talk ends.
            talkToTheNpcNextToTheLeader()
            // Then: an announcement announces the quest, and the marker goes.
            val announcementView = requireNotNull(questAnnouncementView())
            waitUntil { announcementView.isAnnouncementShown() }
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "Bats in the Meadow"
            announcementView.displayedTask() shouldBe "Defeat 3 Bats"
            withClue("Expected no marker over the farmer") { battlefieldView()?.hasQuestMarkerAt(FARMER_ROW, FARMER_COLUMN)?.shouldBeFalse() }
            val trackerView = requireNotNull(questTrackerView())
            withClue("Expected the quest not to take the tracker") { trackerView.displayedTask() shouldBe "Defeat the Goat Wizard" }
            // When: the tracker opens the journal.
            clickCapsule(trackerView)
            val journalView = requireNotNull(questJournalView())
            waitUntil { journalView.isShown() }
            // Then
            journalView.displayedRows().shouldContainExactly(goatWizardQuest, batsQuest)
            // When: the quest is pinned.
            clickPin(journalView, batsQuest)
            waitUntil { trackerView.displayedTask() == "Defeat 3 Bats" }
            // Then
            trackerView.displayedCount() shouldBe "0/3"
            withClue("Expected the pin on the tracker") { trackerView.isPinShown().shouldBeTrue() }
            withClue("Expected the pin of the row in gold") { journalView.isPinnedRow(batsQuest).shouldBeTrue() }
            // When: Back steps out.
            clickAction(requireNotNull(stage.callToActionView()), "Back")
            waitUntil { virtualPadView()?.isPadShown() == true }
            // And the third bat is defeated.
            saveAndQuit()
            countTwoBatsInTheSave()
            loadSlot()
            val loadedTrackerView = requireNotNull(questTrackerView())
            loadedTrackerView.displayedCount() shouldBe "2/3"
            defeatTheGateBat()
            waitUntil { loadedTrackerView.displayedTask() == "Talk to the Farmer" }
            waitUntil { virtualPadView()?.isPadShown() == true && bannerView()?.roundText() == "Exploring" }
            // Then
            withClue("Expected the green marker over the farmer") { battlefieldView()?.questMarkerColorAt(FARMER_ROW, FARMER_COLUMN) shouldBe HudStyle.ALLY }
            // When: the party walks back to the farmer, and talks to it.
            repeat(LEADER_COLUMN_IN_BATTLE - KNIGHT_START_COLUMN) { step ->
                clickArrow(requireNotNull(virtualPadView()), Direction.WEST)
                waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, LEADER_COLUMN_IN_BATTLE - step - 1) == true }
            }
            talkToTheNpcNextToTheLeader()
            // Then: an announcement announces the reward, the marker goes, and the tracker returns to the main quest.
            val loadedAnnouncementView = requireNotNull(questAnnouncementView())
            waitUntil { loadedAnnouncementView.isAnnouncementShown() && loadedAnnouncementView.displayedTitle() == "QUEST COMPLETED" }
            loadedAnnouncementView.displayedName() shouldBe "Bats in the Meadow"
            loadedAnnouncementView.displayedGold().shouldContainExactly("40 G")
            waitUntil { loadedTrackerView.displayedTask() == "Defeat the Goat Wizard" }
            withClue("Expected no pin once it returns to the main quest") { loadedTrackerView.isPinShown().shouldBeFalse() }
            withClue("Expected no marker over the farmer") { battlefieldView()?.hasQuestMarkerAt(FARMER_ROW, FARMER_COLUMN)?.shouldBeFalse() }
        }

    @Test
    fun `should take a side quest back to not started when it is abandoned`() =
        sharedViewsTest {
            // Given
            startWithTheFarmerNextToTheKnight()
            talkToTheNpcNextToTheLeader()
            clickCapsule(requireNotNull(questTrackerView()))
            val journalView = requireNotNull(questJournalView())
            waitUntil { journalView.isShown() }
            clickRow(journalView, batsQuest)
            val detailView = requireNotNull(questDetailView())
            waitUntil { detailView.isDetailShown() }
            // When: Abandon asks first, and Back returns to the detail view.
            clickAction(requireNotNull(stage.callToActionView()), "Abandon")
            val confirmDialogView = requireNotNull(confirmDialogView())
            waitUntil { confirmDialogView.isShown() }
            clickCancelConfirmation(requireNotNull(stage.callToActionView()))
            waitUntil { detailView.isDetailShown() }
            // And it is abandoned.
            clickAction(requireNotNull(stage.callToActionView()), "Abandon")
            waitUntil { confirmDialogView.isShown() }
            clickConfirmation(requireNotNull(stage.callToActionView()))
            waitUntil { virtualPadView()?.isPadShown() == true }
            // Then: no side quest is listed, so the pad is back, and its quest giver can start it again.
            withClue("Expected the gold marker back over the farmer") { battlefieldView()?.questMarkerColorAt(FARMER_ROW, FARMER_COLUMN) shouldBe HudStyle.SELECTION }
            // When
            clickCapsule(requireNotNull(questTrackerView()))
            waitUntil { detailView.isDetailShown() }
            // Then: with no side quest listed, the tracker opens the main quest.
            detailView.displayedCaption() shouldBe "MAIN QUEST"
        }

    @Test
    fun `should restore the pinned quest and the markers when a game is loaded`() =
        sharedViewsTest {
            // Given
            startWithTheFarmerNextToTheKnight()
            talkToTheNpcNextToTheLeader()
            val trackerView = requireNotNull(questTrackerView())
            clickCapsule(trackerView)
            val journalView = requireNotNull(questJournalView())
            waitUntil { journalView.isShown() }
            clickPin(journalView, batsQuest)
            waitUntil { trackerView.displayedTask() == "Defeat 3 Bats" }
            clickAction(requireNotNull(stage.callToActionView()), "Back")
            waitUntil { virtualPadView()?.isPadShown() == true }
            saveAndQuit()
            // When
            loadSlot()
            // Then
            val loadedTrackerView = requireNotNull(questTrackerView())
            loadedTrackerView.displayedTask() shouldBe "Defeat 3 Bats"
            withClue("Expected the pin on the tracker") { loadedTrackerView.isPinShown().shouldBeTrue() }
            withClue("Expected no marker over the farmer, whose quest is under way") { battlefieldView()?.hasQuestMarkerAt(FARMER_ROW, FARMER_COLUMN)?.shouldBeFalse() }
            withClue("Expected the gold marker over the elder") { battlefieldView()?.questMarkerColorAt(ELDER_ROW, ELDER_COLUMN) shouldBe HudStyle.SELECTION }
            withClue("Expected no announcement in a loaded game") { questAnnouncementView()?.isAnnouncementShown()?.shouldBeFalse() }
        }
}
