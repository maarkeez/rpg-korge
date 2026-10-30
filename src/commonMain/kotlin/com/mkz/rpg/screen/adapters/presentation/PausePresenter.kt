package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.party.adapters.presentation.PartyApi

/**
 * The pause screen, opened with Start while exploring, in place of the pad: time stops until Resume. Quit to menu asks
 * to confirm first, as the progress since the last save is lost, and goes back to the menu ([onQuit]).
 */
class PausePresenter(
    private val pauseView: PauseView,
    private val confirmDialogView: ConfirmDialogView,
    private val partyApi: PartyApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    private val onQuit: () -> Unit,
) : PauseView.Delegate {
    /** Whether the scene displays the pause screen, or one of its dialogs. */
    var isOpen = false
        private set

    init {
        pauseView.visible = false
        confirmDialogView.visible = false
        pauseView.setDelegate(this)
    }

    /** Displays the pause screen. To be played in the timeline. */
    fun open() {
        isOpen = true
        displayPause()
    }

    /** Hides the pause screen and its dialogs. Nothing happens when it is not open. */
    fun close() {
        if (!isOpen) return
        isOpen = false
        pauseView.visible = false
        confirmDialogView.visible = false
    }

    override fun pauseResumeSelected() = whenIdle { partyApi.closePause() }

    override fun pauseSaveSelected() = Unit

    override fun pauseQuitSelected() =
        whenIdle {
            confirmDialogView.display(
                message = texts[UiText.PAUSE_QUIT_CONFIRMATION],
                cancelLabel = texts[UiText.CALL_TO_ACTION_CANCEL],
                confirmLabel = texts[UiText.PAUSE_QUIT],
                onCancelled = { displayPause() },
                onConfirmed = { onQuit() },
            )
            pauseView.visible = false
            confirmDialogView.visible = true
        }

    private fun displayPause() {
        confirmDialogView.visible = false
        pauseView.visible = true
    }

    // The player can only act once the scene shows the pause screen.
    private fun whenIdle(action: () -> Unit) {
        if (isOpen && !timeline.isPlaying) action()
    }
}
