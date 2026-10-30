package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.position

/**
 * The pause screen, displayed in place of the pad while the game is paused: Resume, its primary action, Save and Quit
 * to menu, one under the other, centred.
 */
open class PauseView(
    texts: Texts,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val HEIGHT = 300.0
        private const val BUTTON_WIDTH = 250.0
        private const val BUTTON_HEIGHT = 32.0
        private const val BUTTON_GAP = 15.0
    }

    interface Delegate {
        fun pauseResumeSelected()

        fun pauseSaveSelected()

        fun pauseQuitSelected()
    }

    private var delegate: Delegate? = null

    internal val resumeButton = ConfirmButton(label = texts[UiText.PAUSE_RESUME], buttonWidth = BUTTON_WIDTH, onConfirmed = { delegate?.pauseResumeSelected() })
    internal val saveButton = CancelButton(label = texts[UiText.PAUSE_SAVE], buttonWidth = BUTTON_WIDTH, onCancelled = { delegate?.pauseSaveSelected() })
    internal val quitButton = CancelButton(label = texts[UiText.PAUSE_QUIT], buttonWidth = BUTTON_WIDTH, onCancelled = { delegate?.pauseQuitSelected() })

    init {
        uiVerticalStack(padding = BUTTON_GAP) {
            position((WIDTH - BUTTON_WIDTH) / 2, (HEIGHT - 3 * BUTTON_HEIGHT - 2 * BUTTON_GAP) / 2)
            addChild(resumeButton)
            addChild(saveButton)
            addChild(quitButton)
        }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }
}
