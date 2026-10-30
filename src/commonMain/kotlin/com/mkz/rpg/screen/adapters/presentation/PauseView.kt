package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.addUpdater
import korlibs.korge.view.position
import korlibs.math.geom.Size
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The pause screen, displayed in place of the pad while the game is paused: Resume, its primary action, Save and Quit
 * to menu, one under the other, centred. Once the game is saved, Saved is displayed under them for [SAVED_DURATION].
 */
open class PauseView(
    private val texts: Texts,
) : Container() {
    companion object {
        val SAVED_DURATION = 1.5.seconds

        private const val WIDTH = 390.0
        private const val HEIGHT = 300.0
        private const val BUTTON_WIDTH = 250.0
        private const val BUTTON_HEIGHT = 32.0
        private const val BUTTON_GAP = 15.0
        private const val SAVED_HEIGHT = 30.0
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

    private val buttonsY = (HEIGHT - 3 * BUTTON_HEIGHT - 2 * BUTTON_GAP) / 2

    internal val savedText: UIText =
        uiText(texts[UiText.PAUSE_SAVED], size = Size(WIDTH, SAVED_HEIGHT)) {
            styles.textAlignment = TextAlignment.MIDDLE_CENTER
            position(0.0, buttonsY + 3 * BUTTON_HEIGHT + 3 * BUTTON_GAP)
            visible = false
        }

    // The time Saved has been displayed, until it is hidden.
    private var savedElapsed: Duration? = null

    init {
        uiVerticalStack(padding = BUTTON_GAP) {
            position((WIDTH - BUTTON_WIDTH) / 2, buttonsY)
            addChild(resumeButton)
            addChild(saveButton)
            addChild(quitButton)
        }
        addUpdater { elapsed ->
            val displayed = savedElapsed?.plus(elapsed) ?: return@addUpdater
            savedElapsed = displayed
            if (displayed >= SAVED_DURATION) displaySaved(isDisplayed = false)
        }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Displays Saved under the options, which hides itself after [SAVED_DURATION], or hides it straight away. */
    open fun displaySaved(isDisplayed: Boolean) {
        savedText.visible = isDisplayed
        savedElapsed = if (isDisplayed) Duration.ZERO else null
    }
}
