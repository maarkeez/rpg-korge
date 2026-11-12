package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.Save.Dto.SlotStateDto
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.time.DateTime
import kotlin.test.Test

class LoadGamePresenterTest {
    private val texts = MessagesMother.messages()
    private val storage = InmemoryStorage()
    private val saveApi =
        SaveApi(
            storage = storage,
            eventBus = InMemoryEventBus(),
            snapshottables = emptyList(),
            canSave = { false },
            leaderUnitId = { null },
            partySize = { 0 },
            now = { DateTime.nowUnixMillis() },
        )
    private val menuView = MenuView(texts, onNewGameSelected = {}, onLoadGameSelected = {}, onWorldBuilderSelected = {}, onPrivacySelected = {})
    private val saveSlotsView = SaveSlotsView(texts)
    private val confirmDialogView = ConfirmDialogView()
    private val callToActionView = FakeCallToActionView()
    private val loadedSlots = mutableListOf<Int>()

    init {
        storage["save.slot2.summary"] = """{"version":${Save.SAVE_VERSION},"savedAt":0.0,"leaderUnitId":"knight","partySize":3}"""
        storage["save.slot2"] = """{"summary":{"version":${Save.SAVE_VERSION},"savedAt":0.0,"leaderUnitId":"knight","partySize":3},"snapshots":{}}"""
        storage["save.slot3.summary"] = """{"version":0,"savedAt":0.0,"leaderUnitId":"knight","partySize":1}"""
    }

    private fun presenter(incompatibleSlot: Int? = null) =
        LoadGamePresenter(menuView, saveSlotsView, confirmDialogView, callToActionView, saveApi, texts, incompatibleSlot) { slot ->
            loadedSlots +=
                slot
        }

    private fun slotStates() = saveApi.searchSaveSlots().map(Save.Dto.SlotDto::state)

    @Test
    fun `Open should list the save slots in place of the options, only a saved one to load, the ones not empty to delete`() {
        // Given
        val presenter = presenter()
        // When
        presenter.open()
        // Then
        menuView.visible.shouldBeFalse()
        saveSlotsView.visible.shouldBeTrue()
        saveSlotsView.title() shouldBe "Load a game"
        saveSlotsView.slotLabels()[0] shouldBe "Empty"
        saveSlotsView.slotLabels()[1] shouldEndWith "Knight · party of 3"
        saveSlotsView.slotLabels()[2] shouldBe "Incompatible save"
        saveSlotsView.enabledSlots().shouldContainExactly(false, true, false)
        saveSlotsView.deletableSlots().shouldContainExactly(2, 3)
    }

    @Test
    fun `LoadGamePresenter should load the game when a saved slot is chosen`() {
        // Given
        val presenter = presenter().apply { open() }
        // When
        presenter.saveSlotSelected(2)
        // Then
        loadedSlots.shouldContainExactly(2)
    }

    @Test
    fun `LoadGamePresenter should not load when the slot is incompatible`() {
        // Given
        val presenter = presenter().apply { open() }
        // When
        presenter.saveSlotSelected(3)
        // Then
        loadedSlots.shouldBeEmpty()
    }

    @Test
    fun `LoadGamePresenter should list the slots straight away, the one that could not be loaded as incompatible, when it is created with it`() {
        // When
        presenter(incompatibleSlot = 2).saveSlotSelected(2)
        // Then
        saveSlotsView.visible.shouldBeTrue()
        saveSlotsView.slotLabels()[1] shouldBe "Incompatible save"
        loadedSlots.shouldBeEmpty()
    }

    @Test
    fun `LoadGamePresenter should ask to confirm when Delete is tapped`() {
        // Given
        val presenter = presenter().apply { open() }
        // When
        presenter.deleteSaveSelected(2)
        // Then
        confirmDialogView.visible.shouldBeTrue()
        saveSlotsView.visible.shouldBeFalse()
        confirmDialogView.message() shouldBe "Delete the game saved in slot 2?"
        callToActionView.actionLabels().shouldContainExactly("Cancel", "Delete")
    }

    @Test
    fun `LoadGamePresenter should empty the slot when its deletion is confirmed`() {
        // Given
        val presenter = presenter().apply { open() }
        presenter.deleteSaveSelected(2)
        // When
        callToActionView.chooseAction("Delete")
        // Then
        slotStates().shouldContainExactly(SlotStateDto.EMPTY, SlotStateDto.EMPTY, SlotStateDto.INCOMPATIBLE)
        saveSlotsView.visible.shouldBeTrue()
        saveSlotsView.slotLabels()[1] shouldBe "Empty"
    }

    @Test
    fun `LoadGamePresenter should keep the slot when its deletion is cancelled`() {
        // Given
        val presenter = presenter().apply { open() }
        presenter.deleteSaveSelected(2)
        // When
        callToActionView.chooseAction("Cancel")
        // Then
        slotStates()[1] shouldBe SlotStateDto.SAVED
        saveSlotsView.visible.shouldBeTrue()
    }

    @Test
    fun `deleteSaveSelected should display the question's actions, and Cancel display Back again`() {
        // Given
        val presenter = presenter().apply { open() }
        presenter.deleteSaveSelected(2)
        // When
        callToActionView.chooseAction("Cancel")
        // Then
        callToActionView.actionLabels().shouldContainExactly("Back")
    }

    @Test
    fun `Back should hide the row when the menu is displayed again`() {
        // Given
        presenter().open()
        // When
        callToActionView.chooseAction("Back")
        // Then
        saveSlotsView.visible.shouldBeFalse()
        menuView.visible.shouldBeTrue()
        menuView.areOptionsShown().shouldBeTrue()
        callToActionView.displayedActions shouldBe null
    }
}
