package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.save.adapters.presentation.SaveApi
import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.telemetry.domain.TelemetryEvent

class PausePresenter(
    private val pauseView: PauseView,
    private val saveSlotsView: SaveSlotsView,
    private val confirmDialogView: ConfirmDialogView,
    private val callToActionView: CallToActionView,
    private val partyApi: PartyApi,
    private val saveApi: SaveApi,
    private val messages: Messages,
    private val timeline: GameSceneTimeline,
    private val eventBus: EventBus,
    private val onQuit: () -> Unit,
) : PauseView.Delegate,
    SaveSlotsView.Delegate {
    var isOpen = false
        private set

    init {
        pauseView.visible = false
        saveSlotsView.visible = false
        confirmDialogView.visible = false
        pauseView.setDelegate(this)
        saveSlotsView.setDelegate(this)
    }

    fun open() {
        isOpen = true
        pauseView.displaySaved(isDisplayed = false)
        displayPause()
    }

    fun close() {
        if (!isOpen) return
        isOpen = false
        pauseView.visible = false
        saveSlotsView.visible = false
        confirmDialogView.visible = false
    }

    override fun pauseResumeSelected() = whenIdle { partyApi.closePause() }

    override fun pauseSaveSelected() = whenIdle { displaySaveSlots() }

    fun quit() = onQuit()

    override fun pauseQuitSelected() =
        whenIdle {
            confirm(
                message = messages[UiMessage.PAUSE_QUIT_CONFIRMATION],
                confirmLabel = messages[UiMessage.PAUSE_QUIT],
                onCancelled = ::displayPause,
                onConfirmed = onQuit,
            )
        }

    override fun saveSlotSelected(number: Int) =
        whenIdle {
            val slot = saveApi.searchSaveSlots().firstOrNull { slot -> slot.slot == number } ?: return@whenIdle
            if (slot.state == Save.Dto.SlotStateDto.EMPTY) {
                save(number)
            } else {
                confirm(
                    message = messages[UiMessage.SAVE_SLOTS_OVERWRITE_CONFIRMATION, "slot" to number],
                    confirmLabel = messages[UiMessage.SAVE_SLOTS_OVERWRITE],
                    onCancelled = ::displaySaveSlots,
                    onConfirmed = { save(number) },
                )
            }
        }

    override fun deleteSaveSelected(number: Int) = Unit

    private fun save(number: Int) {
        saveApi.saveGame(slot = number)
        eventBus.publish(TelemetryEvent.RequestRecordGameSaved(slot = number))
        displayPause()
        pauseView.displaySaved(isDisplayed = true)
    }

    private fun displayPause() {
        saveSlotsView.visible = false
        confirmDialogView.visible = false
        pauseView.visible = true
        callToActionView.hideButtons()
    }

    private fun displaySaveSlots() {
        saveSlotsView.display(title = messages[UiMessage.SAVE_SLOTS_SAVE_TITLE], slots = SaveSlotsMapper.slots(saveApi.searchSaveSlots(), messages, isLoading = false))
        pauseView.visible = false
        confirmDialogView.visible = false
        saveSlotsView.visible = true
        callToActionView.displayButtons(
            listOf(ButtonRowView.Action(messages[UiMessage.SAVE_SLOTS_BACK]) { whenIdle { displayPause() } }),
        )
    }

    private fun confirm(
        message: String,
        confirmLabel: String,
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        confirmDialogView.display(message)
        pauseView.visible = false
        saveSlotsView.visible = false
        confirmDialogView.visible = true
        callToActionView.displayConfirmation(
            cancelLabel = messages[UiMessage.CALL_TO_ACTION_CANCEL],
            confirmLabel = confirmLabel,
            onCancelled = { whenIdle(onCancelled) },
            onConfirmed = { whenIdle(onConfirmed) },
        )
    }

    private fun whenIdle(action: () -> Unit) {
        if (isOpen && !timeline.isPlaying) action()
    }
}
