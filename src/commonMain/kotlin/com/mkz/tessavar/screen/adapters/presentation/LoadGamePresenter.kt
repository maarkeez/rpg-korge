package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.save.adapters.presentation.SaveApi
import com.mkz.tessavar.save.domain.Save.Dto.SlotStateDto

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

    private fun slots() =
        saveApi.searchSaveSlots().map { slot ->
            if (slot.slot == incompatibleSlot && slot.state == SlotStateDto.SAVED) slot.copy(state = SlotStateDto.INCOMPATIBLE, summary = null) else slot
        }
}
