package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.Save

/**
 * The pause screen, opened with Start while exploring, in place of the pad: time stops until Resume.
 *
 * Save lists the save slots in its place: choosing an empty one saves into it straight away, and choosing one holding a
 * game asks to confirm first, as that game is lost. Once saved, the pause screen is displayed again, with Saved. Back
 * goes back to it without saving.
 *
 * Quit to menu asks to confirm first, as the progress since the last save is lost, and goes back to the menu ([onQuit]).
 */
class PausePresenter(
    private val pauseView: PauseView,
    private val saveSlotsView: SaveSlotsView,
    private val confirmDialogView: ConfirmDialogView,
    private val partyApi: PartyApi,
    private val saveApi: SaveApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    private val onQuit: () -> Unit,
) : PauseView.Delegate,
    SaveSlotsView.Delegate {
    /** Whether the scene displays the pause screen, or one of its dialogs. */
    var isOpen = false
        private set

    init {
        pauseView.visible = false
        saveSlotsView.visible = false
        confirmDialogView.visible = false
        pauseView.setDelegate(this)
        saveSlotsView.setDelegate(this)
    }

    /** Displays the pause screen. To be played in the timeline. */
    fun open() {
        isOpen = true
        pauseView.displaySaved(isDisplayed = false)
        displayPause()
    }

    /** Hides the pause screen and its dialogs. Nothing happens when it is not open. */
    fun close() {
        if (!isOpen) return
        isOpen = false
        pauseView.visible = false
        saveSlotsView.visible = false
        confirmDialogView.visible = false
    }

    override fun pauseResumeSelected() = whenIdle { partyApi.closePause() }

    override fun pauseSaveSelected() = whenIdle { displaySaveSlots() }

    override fun pauseQuitSelected() =
        whenIdle {
            confirm(
                message = texts[UiText.PAUSE_QUIT_CONFIRMATION],
                confirmLabel = texts[UiText.PAUSE_QUIT],
                onCancelled = ::displayPause,
                onConfirmed = onQuit,
            )
        }

    // An empty slot saves straight away, and a slot holding a game (or an incompatible one) asks to overwrite it first.
    override fun saveSlotSelected(number: Int) =
        whenIdle {
            val slot = saveApi.searchSaveSlots().firstOrNull { slot -> slot.slot == number } ?: return@whenIdle
            if (slot.state == Save.Dto.SlotStateDto.EMPTY) {
                save(number)
            } else {
                confirm(
                    message = texts[UiText.SAVE_SLOTS_OVERWRITE_CONFIRMATION, "slot" to number],
                    confirmLabel = texts[UiText.SAVE_SLOTS_OVERWRITE],
                    onCancelled = ::displaySaveSlots,
                    onConfirmed = { save(number) },
                )
            }
        }

    // Slots are only deleted from Load game, in the menu.
    override fun saveSlotDeleteSelected(number: Int) = Unit

    override fun saveSlotsBackSelected() = whenIdle { displayPause() }

    private fun save(number: Int) {
        saveApi.saveGame(slot = number)
        displayPause()
        pauseView.displaySaved(isDisplayed = true)
    }

    private fun displayPause() {
        saveSlotsView.visible = false
        confirmDialogView.visible = false
        pauseView.visible = true
    }

    private fun displaySaveSlots() {
        saveSlotsView.display(title = texts[UiText.SAVE_SLOTS_SAVE_TITLE], slots = SaveSlotsMapper.slots(saveApi.searchSaveSlots(), texts, isLoading = false))
        pauseView.visible = false
        confirmDialogView.visible = false
        saveSlotsView.visible = true
    }

    private fun confirm(
        message: String,
        confirmLabel: String,
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        confirmDialogView.display(
            message = message,
            cancelLabel = texts[UiText.CALL_TO_ACTION_CANCEL],
            confirmLabel = confirmLabel,
            onCancelled = onCancelled,
            onConfirmed = onConfirmed,
        )
        pauseView.visible = false
        saveSlotsView.visible = false
        confirmDialogView.visible = true
    }

    // The player can only act once the scene shows the pause screen.
    private fun whenIdle(action: () -> Unit) {
        if (isOpen && !timeline.isPlaying) action()
    }
}
