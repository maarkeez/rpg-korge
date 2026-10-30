package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.Save.Dto.SlotStateDto

/**
 * Load game, from the menu: the save slots in place of its options. Choosing a saved slot loads its game ([onLoad]), and
 * Delete, on any slot that is not empty, deletes its game once confirmed. Back displays the options again.
 *
 * Incompatible slot: a slot whose game could not be loaded (e.g.: it names a unit the game no longer has), shown as an
 * incompatible save, as listing the slots only reads their summaries. Its save slots are then listed straight away.
 */
class LoadGamePresenter(
    private val menuView: MenuView,
    private val saveSlotsView: SaveSlotsView,
    private val confirmDialogView: ConfirmDialogView,
    private val saveApi: SaveApi,
    private val texts: Texts,
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
        onLoad(number)
    }

    override fun saveSlotDeleteSelected(number: Int) {
        confirmDialogView.display(
            message = texts[UiText.SAVE_SLOTS_DELETE_CONFIRMATION, "slot" to number],
            cancelLabel = texts[UiText.CALL_TO_ACTION_CANCEL],
            confirmLabel = texts[UiText.SAVE_SLOTS_DELETE],
            onCancelled = ::displaySlots,
            onConfirmed = { delete(number) },
        )
        saveSlotsView.visible = false
        confirmDialogView.visible = true
    }

    override fun saveSlotsBackSelected() {
        saveSlotsView.visible = false
        menuView.visible = true
        menuView.displayOptions()
    }

    private fun delete(number: Int) {
        saveApi.deleteSaveSlot(slot = number)
        if (number == incompatibleSlot) incompatibleSlot = null
        displaySlots()
    }

    private fun displaySlots() {
        saveSlotsView.display(title = texts[UiText.SAVE_SLOTS_LOAD_TITLE], slots = SaveSlotsMapper.slots(slots(), texts, isLoading = true))
        confirmDialogView.visible = false
        saveSlotsView.visible = true
    }

    // The slot that could not be loaded is incompatible, though its summary reads as saved.
    private fun slots() =
        saveApi.searchSaveSlots().map { slot ->
            if (slot.slot == incompatibleSlot && slot.state == SlotStateDto.SAVED) slot.copy(state = SlotStateDto.INCOMPATIBLE, summary = null) else slot
        }
}
