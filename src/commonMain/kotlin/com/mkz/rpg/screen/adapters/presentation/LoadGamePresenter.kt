package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.Save.Dto.SlotStateDto

/**
 * Load game, from the menu: the save slots in place of its options. Choosing a saved slot loads its game ([onLoad]), and
 * Delete, on any slot that is not empty, deletes its game once confirmed. Back, in the row of the call to action as the
 * choices of the question, displays the options again.
 *
 * Incompatible slot: a slot whose game could not be loaded (e.g.: it names a unit the game no longer has), shown as an
 * incompatible save, as listing the slots only reads their summaries. Its save slots are then listed straight away.
 */
class LoadGamePresenter(
    private val menuView: MenuView,
    private val saveSlotsView: SaveSlotsView,
    private val confirmDialogView: ConfirmDialogView,
    private val callToActionView: CallToActionView,
    private val saveApi: SaveApi,
    private val messages: Messages,
    private var incompatibleSlot: Int?,
    private val onLoad: (slot: Int) -> Unit,
) : SaveSlotsView.Delegate {
    init {
        saveSlotsView.visible = false
        confirmDialogView.visible = false
        saveSlotsView.setDelegate(this)
        if (incompatibleSlot != null) open()
    }

    /** Lists the save slots in place of the options. */
    fun open() {
        menuView.visible = false
        displaySlots()
    }

    override fun saveSlotSelected(number: Int) {
        if (slots().firstOrNull { slot -> slot.slot == number }?.state != SlotStateDto.SAVED) return
        saveSlotsView.visible = false
        callToActionView.hideButtons()
        onLoad(number)
    }

    override fun deleteSaveSelected(number: Int) {
        confirmDialogView.display(messages[UiMessage.SAVE_SLOTS_DELETE_CONFIRMATION, "slot" to number])
        saveSlotsView.visible = false
        confirmDialogView.visible = true
        callToActionView.displayConfirmation(
            cancelLabel = messages[UiMessage.CALL_TO_ACTION_CANCEL],
            confirmLabel = messages[UiMessage.SAVE_SLOTS_DELETE],
            onCancelled = ::displaySlots,
            onConfirmed = { delete(number) },
        )
    }

    // The options of the menu have no row.
    private fun backSelected() {
        saveSlotsView.visible = false
        menuView.visible = true
        menuView.displayOptions()
        callToActionView.hideButtons()
    }

    private fun delete(number: Int) {
        saveApi.deleteSave(slot = number)
        if (number == incompatibleSlot) incompatibleSlot = null
        displaySlots()
    }

    private fun displaySlots() {
        saveSlotsView.display(title = messages[UiMessage.SAVE_SLOTS_LOAD_TITLE], slots = SaveSlotsMapper.slots(slots(), messages, isLoading = true))
        confirmDialogView.visible = false
        saveSlotsView.visible = true
        callToActionView.displayButtons(listOf(ButtonRowView.Action(messages[UiMessage.SAVE_SLOTS_BACK]) { backSelected() }))
    }

    // The slot that could not be loaded is incompatible, though its summary reads as saved.
    private fun slots() =
        saveApi.searchSaveSlots().map { slot ->
            if (slot.slot == incompatibleSlot && slot.state == SlotStateDto.SAVED) slot.copy(state = SlotStateDto.INCOMPATIBLE, summary = null) else slot
        }
}
