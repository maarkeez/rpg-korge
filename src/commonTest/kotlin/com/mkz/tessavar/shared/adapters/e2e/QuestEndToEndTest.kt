package com.mkz.tessavar.shared.adapters.e2e

import com.mkz.tessavar.GAME_WINDOW_SIZE
import com.mkz.tessavar.screen.adapters.presentation.HudStyle
import com.mkz.tessavar.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.tessavar.screen.adapters.presentation.bannerView
import com.mkz.tessavar.screen.adapters.presentation.battleUnitInfoView
import com.mkz.tessavar.screen.adapters.presentation.battlefieldView
import com.mkz.tessavar.screen.adapters.presentation.callToActionView
import com.mkz.tessavar.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.tessavar.screen.adapters.presentation.clickA
import com.mkz.tessavar.screen.adapters.presentation.clickAbility
import com.mkz.tessavar.screen.adapters.presentation.clickAction
import com.mkz.tessavar.screen.adapters.presentation.clickArrow
import com.mkz.tessavar.screen.adapters.presentation.clickCancelConfirmation
import com.mkz.tessavar.screen.adapters.presentation.clickCapsule
import com.mkz.tessavar.screen.adapters.presentation.clickConfirm
import com.mkz.tessavar.screen.adapters.presentation.clickConfirmation
import com.mkz.tessavar.screen.adapters.presentation.clickDialog
import com.mkz.tessavar.screen.adapters.presentation.clickLoadGame
import com.mkz.tessavar.screen.adapters.presentation.clickPin
import com.mkz.tessavar.screen.adapters.presentation.clickQuit
import com.mkz.tessavar.screen.adapters.presentation.clickRow
import com.mkz.tessavar.screen.adapters.presentation.clickSave
import com.mkz.tessavar.screen.adapters.presentation.clickSlot
import com.mkz.tessavar.screen.adapters.presentation.clickStart
import com.mkz.tessavar.screen.adapters.presentation.clickTile
import com.mkz.tessavar.screen.adapters.presentation.confirmDialogView
import com.mkz.tessavar.screen.adapters.presentation.displayedCaption
import com.mkz.tessavar.screen.adapters.presentation.displayedCount
import com.mkz.tessavar.screen.adapters.presentation.displayedGold
import com.mkz.tessavar.screen.adapters.presentation.displayedLine
import com.mkz.tessavar.screen.adapters.presentation.displayedName
import com.mkz.tessavar.screen.adapters.presentation.displayedRows
import com.mkz.tessavar.screen.adapters.presentation.displayedTask
import com.mkz.tessavar.screen.adapters.presentation.displayedTitle
import com.mkz.tessavar.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.tessavar.screen.adapters.presentation.hasQuestMarkerAt
import com.mkz.tessavar.screen.adapters.presentation.hasSelectionAt
import com.mkz.tessavar.screen.adapters.presentation.isAnnouncementShown
import com.mkz.tessavar.screen.adapters.presentation.isChevronShown
import com.mkz.tessavar.screen.adapters.presentation.isDetailShown
import com.mkz.tessavar.screen.adapters.presentation.isDialogShown
import com.mkz.tessavar.screen.adapters.presentation.isDimmedAt
import com.mkz.tessavar.screen.adapters.presentation.isDisplayingCancelAndConfirm
import com.mkz.tessavar.screen.adapters.presentation.isPadShown
import com.mkz.tessavar.screen.adapters.presentation.isPinShown
import com.mkz.tessavar.screen.adapters.presentation.isPinnedRow
import com.mkz.tessavar.screen.adapters.presentation.isSavedShown
import com.mkz.tessavar.screen.adapters.presentation.isTickShown
import com.mkz.tessavar.screen.adapters.presentation.menuView
import com.mkz.tessavar.screen.adapters.presentation.pauseView
import com.mkz.tessavar.screen.adapters.presentation.questAnnouncementView
import com.mkz.tessavar.screen.adapters.presentation.questDetailView
import com.mkz.tessavar.screen.adapters.presentation.questJournalView
import com.mkz.tessavar.screen.adapters.presentation.questMarkerColorAt
import com.mkz.tessavar.screen.adapters.presentation.questTrackerView
import com.mkz.tessavar.screen.adapters.presentation.roundText
import com.mkz.tessavar.screen.adapters.presentation.saveSlotsView
import com.mkz.tessavar.screen.adapters.presentation.talkView
import com.mkz.tessavar.screen.adapters.presentation.virtualPadView
import com.mkz.tessavar.shared.adapters.presentation.isShown
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
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
private const val BLACKSMITH_ROW = 21
private const val BLACKSMITH_COLUMN = 5

private const val NORTH_OF_THE_ENTRANCE_ROW = 13
private const val ENTRANCE_COLUMN = 10

class QuestEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private val swordAbilityIndex = 0
    private val saveSlotKey = "save.slot1"
    private val gateBat = """"id":"gate-bat","unitId":"bat""""
    private val gateBatAsTheGoatWizard = """"id":"gate-bat","unitId":"goat-wizard""""
    private val farmerInTheSave = """"id":"farmer","row":8,"column":12"""
    private val farmerOnTheBattlefieldInTheSave = """"occupantId":"farmer","row":8,"column":12"""
    private val batsQuestStarted = """"questId":"bats-in-the-meadow","status":"ACTIVE","currentTaskIndex":0,"currentTaskDefeats":0"""
    private val batsQuest = "bats-in-the-meadow"
    private val knightOnTheBattlefieldInTheSave = """"occupantId":"hero","row":$KNIGHT_START_ROW,"column":$KNIGHT_START_COLUMN"""
    private val mainQuestNotStarted = """"questId":"defeat-the-goat-wizard","status":"NOT_STARTED","currentTaskIndex":0"""

    private val storage = InmemoryStorage()

    private suspend fun ViewsForTesting.startExploring() {
        startNewGame(storage = storage)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
    }

    private suspend fun ViewsForTesting.saveAndQuit() {
        clickStart(requireNotNull(stage.virtualPadView()))
        waitUntil { stage.pauseView()?.isShown() == true }
        clickSave(requireNotNull(stage.pauseView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.pauseView()?.isSavedShown() == true || stage.confirmDialogView()?.isShown() == true }
        if (stage.confirmDialogView()?.isShown() == true) clickConfirmation(requireNotNull(stage.callToActionView()))
        waitUntil { stage.pauseView()?.isSavedShown() == true }
        clickQuit(requireNotNull(stage.pauseView()))
        waitUntil { stage.confirmDialogView()?.isShown() == true }
        clickConfirmation(requireNotNull(stage.callToActionView()))
        waitUntil { stage.menuView()?.isShown() == true }
    }

    private suspend fun ViewsForTesting.loadSlot(isAQuestActive: Boolean = true) {
        clickLoadGame(requireNotNull(stage.menuView()))
        waitUntil { stage.saveSlotsView()?.isShown() == true }
        clickSlot(requireNotNull(stage.saveSlotsView()), 1)
        waitUntil { stage.virtualPadView()?.isPadShown() == true && (!isAQuestActive || stage.questTrackerView()?.displayedTask() != null) }
    }

    private fun moveTheKnightNorthOfTheEntranceInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain knightOnTheBattlefieldInTheSave
        storage[saveSlotKey] = save.replace(knightOnTheBattlefieldInTheSave, """"occupantId":"hero","row":$NORTH_OF_THE_ENTRANCE_ROW,"column":$ENTRANCE_COLUMN""")
    }

    private fun startTheMainQuestInTheSave(taskIndex: Int = 0) {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain mainQuestNotStarted
        storage[saveSlotKey] = save.replace(mainQuestNotStarted, """"questId":"defeat-the-goat-wizard","status":"ACTIVE","currentTaskIndex":$taskIndex""")
    }

    private suspend fun ViewsForTesting.startWithTheMainQuestStarted(taskIndex: Int = 0) {
        startExploring()
        saveAndQuit()
        startTheMainQuestInTheSave(taskIndex)
        loadSlot()
    }

    private fun moveTheFarmerNextToTheKnightInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain farmerInTheSave
        save shouldContain farmerOnTheBattlefieldInTheSave
        storage[saveSlotKey] =
            save
                .replace(farmerInTheSave, """"id":"farmer","row":$FARMER_ROW,"column":$FARMER_COLUMN""")
                .replace(farmerOnTheBattlefieldInTheSave, """"occupantId":"farmer","row":$FARMER_ROW,"column":$FARMER_COLUMN""")
    }

    private fun countTwoBatsInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain batsQuestStarted
        storage[saveSlotKey] = save.replace(batsQuestStarted, batsQuestStarted.replace(""""currentTaskDefeats":0""", """"currentTaskDefeats":2"""))
    }

    private suspend fun ViewsForTesting.startWithTheFarmerNextToTheKnight() {
        startExploring()
        saveAndQuit()
        moveTheFarmerNextToTheKnightInTheSave()
        loadSlot(isAQuestActive = false)
    }

    private suspend fun ViewsForTesting.talkToTheNpcNextToTheLeader(): String {
        clickA(requireNotNull(stage.virtualPadView()))
        val talkView = requireNotNull(stage.talkView())
        waitUntil { talkView.isDialogShown() }
        val line = talkView.displayedLine()
        clickDialog(talkView)
        waitUntil { stage.virtualPadView()?.isPadShown() == true }
        return line
    }

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

    private fun makeTheGateBatTheGoatWizardInTheSave() {
        val save = requireNotNull(storage.getOrNull(saveSlotKey)) { "Nothing is saved in the slot" }
        save shouldContain gateBat
        storage[saveSlotKey] = save.replace(gateBat, gateBatAsTheGoatWizard)
    }

    @Test
    fun `should play a new game with no announcement and no tracker`() =
        sharedViewsTest {
            // When
            startExploring()
            clickArrow(requireNotNull(virtualPadView()), Direction.EAST)
            waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN + 1) == true }
            // Then
            withClue("Expected no announcement in a new game") { questAnnouncementView()?.isAnnouncementShown()?.shouldBeFalse() }
            withClue("Expected no tracker while no quest is active") { questTrackerView()?.displayedTask() shouldBe null }
        }

    @Test
    fun `should announce the main quest and name Talk to the Elder when the leader walks onto the village entrance`() =
        sharedViewsTest {
            // Given
            startExploring()
            saveAndQuit()
            moveTheKnightNorthOfTheEntranceInTheSave()
            loadSlot(isAQuestActive = false)
            // When
            clickArrow(requireNotNull(virtualPadView()), Direction.SOUTH)
            val announcementView = requireNotNull(questAnnouncementView())
            waitUntil { announcementView.isAnnouncementShown() }
            // Then
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "The Goat Wizard's Lair"
            announcementView.displayedTask() shouldBe "Talk to the Elder"
            requireNotNull(questTrackerView()).displayedTask() shouldBe "Talk to the Elder"
            withClue("Expected the green marker over the elder") { battlefieldView()?.questMarkerColorAt(ELDER_ROW, ELDER_COLUMN) shouldBe HudStyle.ALLY }
        }

    @Test
    fun `should open the detail view in place of the pad when the tracker is tapped, and display the pad again with Back`() =
        sharedViewsTest {
            // Given
            startWithTheMainQuestStarted()
            // When
            clickCapsule(requireNotNull(questTrackerView()))
            waitUntil { questDetailView()?.isDetailShown() == true }
            // Then
            withClue("Expected the detail view to be displayed") { questDetailView()?.isDetailShown()?.shouldBeTrue() }
            withClue("Expected the virtual pad to be hidden while reading the quest") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
            questDetailView()?.displayedTask() shouldBe "Talk to the Elder"
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
            // Given
            startExploring()
            saveAndQuit()
            makeTheGateBatTheGoatWizardInTheSave()
            startTheMainQuestInTheSave(taskIndex = 1)
            loadSlot()
            val trackerView = requireNotNull(questTrackerView())
            // When
            defeatTheGateBat()
            val announcementView = requireNotNull(questAnnouncementView())
            waitUntil { announcementView.isAnnouncementShown() }
            // Then
            withClue("Expected the announcement to announce the completion") { announcementView.isAnnouncementShown().shouldBeTrue() }
            announcementView.displayedTitle() shouldBe "QUEST COMPLETED"
            announcementView.displayedGold().shouldContainExactly("150 G")
            trackerView.displayedTask() shouldBe "Quest completed"
            withClue("Expected a ticked tracker") { trackerView.isTickShown().shouldBeTrue() }
            // When
            waitUntil { virtualPadView()?.isPadShown() == true && bannerView()?.roundText() == "Exploring" }
            // Then
            withClue("Expected the chevron back once exploring") { trackerView.isChevronShown().shouldBeTrue() }
            clickCapsule(trackerView)
            waitUntil { questDetailView()?.isDetailShown() == true }
            questDetailView()?.displayedCaption() shouldBe "QUEST COMPLETED"
        }

    @Test
    fun `should not announce and should display the tracker when a game is loaded`() =
        sharedViewsTest {
            // Given
            startWithTheMainQuestStarted()
            saveAndQuit()
            // When
            loadSlot()
            // Then
            requireNotNull(questTrackerView()).displayedTask() shouldBe "Talk to the Elder"
            withClue("Expected no announcement in a loaded game") { questAnnouncementView()?.isAnnouncementShown()?.shouldBeFalse() }
        }

    @Test
    fun `should play a side quest when it is started, pinned, progressed and completed`() =
        sharedViewsTest {
            // Given
            startWithTheFarmerNextToTheKnight()
            withClue("Expected the gold marker over the farmer") { battlefieldView()?.questMarkerColorAt(FARMER_ROW, FARMER_COLUMN) shouldBe HudStyle.SELECTION }
            // When
            talkToTheNpcNextToTheLeader()
            // Then
            val announcementView = requireNotNull(questAnnouncementView())
            waitUntil { announcementView.isAnnouncementShown() }
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "Bats in the Meadow"
            announcementView.displayedTask() shouldBe "Defeat 3 Bats"
            withClue("Expected no marker over the farmer") { battlefieldView()?.hasQuestMarkerAt(FARMER_ROW, FARMER_COLUMN)?.shouldBeFalse() }
            val trackerView = requireNotNull(questTrackerView())
            withClue("Expected the empty tracker, as the quest does not take it") { trackerView.displayedTask() shouldBe "No quest pinned" }
            // When
            clickCapsule(trackerView)
            val journalView = requireNotNull(questJournalView())
            waitUntil { journalView.isShown() }
            // Then
            journalView.displayedRows().shouldContainExactly(batsQuest)
            // When
            clickPin(journalView, batsQuest)
            waitUntil { trackerView.displayedTask() == "Defeat 3 Bats" }
            // Then
            trackerView.displayedCount() shouldBe "0/3"
            withClue("Expected the pin on the tracker") { trackerView.isPinShown().shouldBeTrue() }
            withClue("Expected the pin of the row in gold") { journalView.isPinnedRow(batsQuest).shouldBeTrue() }
            // When
            clickAction(requireNotNull(stage.callToActionView()), "Back")
            waitUntil { virtualPadView()?.isPadShown() == true }
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
            // When
            repeat(LEADER_COLUMN_IN_BATTLE - KNIGHT_START_COLUMN) { step ->
                clickArrow(requireNotNull(virtualPadView()), Direction.WEST)
                waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, LEADER_COLUMN_IN_BATTLE - step - 1) == true }
            }
            val line = talkToTheNpcNextToTheLeader()
            // Then
            line shouldBe "Three bats gone! The meadow is quiet at last. Here, take this gold: you have earned it."
            val loadedAnnouncementView = requireNotNull(questAnnouncementView())
            waitUntil { loadedAnnouncementView.isAnnouncementShown() && loadedAnnouncementView.displayedTitle() == "QUEST COMPLETED" }
            loadedAnnouncementView.displayedName() shouldBe "Bats in the Meadow"
            loadedAnnouncementView.displayedGold().shouldContainExactly("40 G")
            waitUntil { loadedTrackerView.displayedTask() == null }
            withClue("Expected no pin once no quest is active") { loadedTrackerView.isPinShown().shouldBeFalse() }
            withClue("Expected no marker over the farmer") { battlefieldView()?.hasQuestMarkerAt(FARMER_ROW, FARMER_COLUMN)?.shouldBeFalse() }
        }

    @Test
    fun `should say the start line, then the reminder line, when the farmer is talked to twice`() =
        sharedViewsTest {
            // Given
            startWithTheFarmerNextToTheKnight()
            // When
            val startLine = talkToTheNpcNextToTheLeader()
            val reminderLine = talkToTheNpcNextToTheLeader()
            // Then
            startLine shouldBe
                "Thank you for dealing with that bat! More of them nest in my meadow, off the path. Defeat three of them and I will pay you well. My village is just south."
            reminderLine shouldBe "The bats still nest in my meadow, off the path. Three less of them, and I can sleep again."
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
            // When
            clickAction(requireNotNull(stage.callToActionView()), "Abandon")
            val confirmDialogView = requireNotNull(confirmDialogView())
            waitUntil { confirmDialogView.isShown() }
            clickCancelConfirmation(requireNotNull(stage.callToActionView()))
            waitUntil { detailView.isDetailShown() }
            clickAction(requireNotNull(stage.callToActionView()), "Abandon")
            waitUntil { confirmDialogView.isShown() }
            clickConfirmation(requireNotNull(stage.callToActionView()))
            waitUntil { virtualPadView()?.isPadShown() == true }
            // Then
            withClue("Expected the gold marker back over the farmer") { battlefieldView()?.questMarkerColorAt(FARMER_ROW, FARMER_COLUMN) shouldBe HudStyle.SELECTION }
            withClue("Expected no tracker, as no quest is active") { questTrackerView()?.displayedTask() shouldBe null }
        }

    @Test
    fun `should keep the pinned quest and the markers when a game is loaded`() =
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
            withClue("Expected the gold marker over the blacksmith") { battlefieldView()?.questMarkerColorAt(BLACKSMITH_ROW, BLACKSMITH_COLUMN) shouldBe HudStyle.SELECTION }
            withClue("Expected no announcement in a loaded game") { questAnnouncementView()?.isAnnouncementShown()?.shouldBeFalse() }
        }
}
