package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.Save.Dto.SlotStateDto
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.time.DateTime
import kotlin.test.Test

class PausePresenterTest {
    // The knight explores the sand, alone.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto()),
            tiles = List(2) { List(2) { "sand" } },
            deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 0, column = 0)),
            explore = true,
        ).also(SubdomainApis::initialise)
    private val saveApi =
        SaveApi(
            storage = InmemoryStorage(),
            eventBus = apis.eventBus,
            snapshottables = listOf(apis.battleUnitApi.snapshottable, apis.partyApi.snapshottable),
            canSave = { apis.partyApi.searchParty()?.isPaused == true },
            leaderUnitId = { "knight" },
            partySize = { 1 },
            now = { DateTime.nowUnixMillis() },
        )
    private val pauseView = PauseView(TextsMother.texts())
    private val saveSlotsView = SaveSlotsView(TextsMother.texts())
    private val confirmDialogView = ConfirmDialogView()
    private val callToActionView = FakePlayerCallToActionView()
    private var quits = 0
    private val presenter = PausePresenter(pauseView, saveSlotsView, confirmDialogView, callToActionView, apis.partyApi, saveApi, TextsMother.texts(), battleSceneTimeline()) { quits++ }

    private fun openPause() {
        apis.partyApi.openPause()
        apis.eventBus.dispatch()
        presenter.open()
    }

    private fun slotStates() = saveApi.searchSaveSlots().map { slot -> slot.state }

    @Test
    fun `Open should display the pause screen when the game is paused`() {
        // When
        openPause()
        // Then
        presenter.isOpen.shouldBeTrue()
        pauseView.visible.shouldBeTrue()
        saveSlotsView.visible.shouldBeFalse()
        confirmDialogView.visible.shouldBeFalse()
    }

    @Test
    fun `PauseResumeSelected should explore again when Resume is tapped`() {
        // Given
        openPause()
        // When
        presenter.pauseResumeSelected()
        // Then
        apis.partyApi
            .searchParty()!!
            .isExploring
            .shouldBeTrue()
    }

    @Test
    fun `PauseSaveSelected should list the save slots in place of the pause screen when Save is tapped`() {
        // Given
        openPause()
        // When
        presenter.pauseSaveSelected()
        // Then
        saveSlotsView.visible.shouldBeTrue()
        pauseView.visible.shouldBeFalse()
        saveSlotsView.title() shouldBe "Save the game"
        saveSlotsView.slotLabels().shouldContainExactly("Empty", "Empty", "Empty")
        saveSlotsView.enabledSlots().shouldContainExactly(true, true, true)
    }

    @Test
    fun `PausePresenter should show Saved and the pause screen when the game is saved`() {
        // Given
        openPause()
        presenter.pauseSaveSelected()
        // When
        presenter.saveSlotSelected(2)
        // Then
        slotStates().shouldContainExactly(SlotStateDto.EMPTY, SlotStateDto.SAVED, SlotStateDto.EMPTY)
        pauseView.visible.shouldBeTrue()
        pauseView.isSavedShown().shouldBeTrue()
        saveSlotsView.visible.shouldBeFalse()
    }

    @Test
    fun `PausePresenter should ask to confirm when saving into a taken slot`() {
        // Given
        openPause()
        presenter.pauseSaveSelected()
        presenter.saveSlotSelected(2)
        presenter.pauseSaveSelected()
        // When
        presenter.saveSlotSelected(2)
        // Then
        confirmDialogView.visible.shouldBeTrue()
        confirmDialogView.message() shouldBe "Overwrite the game saved in slot 2?"
        callToActionView.actionLabels().shouldContainExactly("Cancel", "Overwrite")
        saveSlotsView.slotLabels()[1] shouldEndWith "Knight · party of 1"
    }

    @Test
    fun `PausePresenter should save over the slot when overwriting it is confirmed`() {
        // Given
        openPause()
        presenter.pauseSaveSelected()
        presenter.saveSlotSelected(2)
        presenter.pauseSaveSelected()
        presenter.saveSlotSelected(2)
        // When
        callToActionView.chooseAction("Overwrite")
        // Then
        pauseView.isSavedShown().shouldBeTrue()
        confirmDialogView.visible.shouldBeFalse()
    }

    @Test
    fun `PausePresenter should list the save slots again when overwriting is cancelled`() {
        // Given
        openPause()
        presenter.pauseSaveSelected()
        presenter.saveSlotSelected(2)
        presenter.pauseSaveSelected()
        presenter.saveSlotSelected(2)
        // When
        callToActionView.chooseAction("Cancel")
        // Then
        saveSlotsView.visible.shouldBeTrue()
        confirmDialogView.visible.shouldBeFalse()
        callToActionView.actionLabels().shouldContainExactly("Back")
    }

    @Test
    fun `Save should display Back, and Back hide it again`() {
        // Given
        openPause()
        // When
        presenter.pauseSaveSelected()
        // Then
        callToActionView.actionLabels().shouldContainExactly("Back")
        // When
        callToActionView.chooseAction("Back")
        // Then
        pauseView.visible.shouldBeTrue()
        saveSlotsView.visible.shouldBeFalse()
        slotStates().shouldContainExactly(SlotStateDto.EMPTY, SlotStateDto.EMPTY, SlotStateDto.EMPTY)
        callToActionView.displayedActions shouldBe null
    }

    @Test
    fun `pauseQuitSelected should display the question's actions, and Cancel hide them again`() {
        // Given
        openPause()
        presenter.pauseQuitSelected()
        // When
        callToActionView.chooseAction("Cancel")
        // Then
        callToActionView.displayedActions shouldBe null
    }

    @Test
    fun `PausePresenter should do nothing when a choice is tapped while the timeline is playing`() {
        // Given
        val timeline = battleSceneTimeline()
        val presenter = PausePresenter(pauseView, saveSlotsView, confirmDialogView, callToActionView, apis.partyApi, saveApi, TextsMother.texts(), timeline) { quits++ }
        apis.partyApi.openPause()
        apis.eventBus.dispatch()
        presenter.open()
        presenter.pauseQuitSelected()
        timeline.playLongStep()
        // When
        callToActionView.chooseAction("Quit to menu")
        // Then
        quits shouldBe 0
    }

    @Test
    fun `PauseQuitSelected should ask to confirm, as the progress since the last save is lost, when Quit to menu is tapped`() {
        // Given
        openPause()
        // When
        presenter.pauseQuitSelected()
        // Then
        pauseView.visible.shouldBeFalse()
        confirmDialogView.visible.shouldBeTrue()
        confirmDialogView.message() shouldBe "Quit to the menu? The progress since the last save is lost."
        callToActionView.actionLabels().shouldContainExactly("Cancel", "Quit to menu")
        quits shouldBe 0
    }

    @Test
    fun `PauseQuitSelected should go back to the menu when quitting is confirmed`() {
        // Given
        openPause()
        presenter.pauseQuitSelected()
        // When
        callToActionView.chooseAction("Quit to menu")
        // Then
        quits shouldBe 1
    }

    @Test
    fun `PauseQuitSelected should display the pause screen again when quitting is cancelled`() {
        // Given
        openPause()
        presenter.pauseQuitSelected()
        // When
        callToActionView.chooseAction("Cancel")
        // Then
        pauseView.visible.shouldBeTrue()
        confirmDialogView.visible.shouldBeFalse()
        quits shouldBe 0
    }

    @Test
    fun `Close should hide the pause screen and its dialogs`() {
        // Given
        openPause()
        presenter.pauseSaveSelected()
        // When
        presenter.close()
        // Then
        presenter.isOpen.shouldBeFalse()
        pauseView.visible.shouldBeFalse()
        saveSlotsView.visible.shouldBeFalse()
        confirmDialogView.visible.shouldBeFalse()
    }
}
