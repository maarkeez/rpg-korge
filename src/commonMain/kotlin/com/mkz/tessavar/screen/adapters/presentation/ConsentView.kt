package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.HudStyle.panel
import com.mkz.tessavar.shared.adapters.presentation.GameButton
import com.mkz.tessavar.shared.adapters.presentation.gameButton
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.DefaultTtfFontAsBitmap
import korlibs.image.text.RichTextData
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.TextBlock
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.textBlock
import korlibs.korge.view.xy
import korlibs.math.geom.Size

class ConsentView(
    private val messages: Messages,
) : Container() {
    interface Delegate {
        fun consentAccepted()

        fun consentDeclined()

        fun consentReadMoreSelected()
    }

    companion object {
        private const val SCREEN_WIDTH = 390.0
        private const val SCREEN_HEIGHT = 844.0
        private const val PANEL_WIDTH = 350.0
        private const val PANEL_HEIGHT = 284.0
        private const val PANEL_RADIUS = 12.0
        private const val PADDING = 20.0
        private const val TITLE_SIZE = 20.0
        private const val BODY_Y = 56.0
        private const val BODY_HEIGHT = 120.0
        private const val TEXT_SIZE = 15.0
        private const val READ_MORE_Y = BODY_Y + BODY_HEIGHT + 10.0
        private const val BUTTON_SPACING = 10.0

        val DIM = RGBA(0, 0, 0, 160)
    }

    private var delegate: Delegate? = null

    init {
        solidRect(SCREEN_WIDTH, SCREEN_HEIGHT, DIM).onClick { }
    }

    private val content =
        container {
            xy((SCREEN_WIDTH - PANEL_WIDTH) / 2, (SCREEN_HEIGHT - PANEL_HEIGHT) / 2)
            panel(Size(PANEL_WIDTH, PANEL_HEIGHT), radius = PANEL_RADIUS, stroke = HudStyle.SELECTION)
        }

    internal val titleText: Text = content.text("", textSize = TITLE_SIZE, color = Colors.WHITE) { xy(PADDING, PADDING) }

    internal val bodyText: TextBlock =
        content.textBlock(RichTextData(""), align = TextAlignment.TOP_LEFT, size = Size(PANEL_WIDTH - 2 * PADDING, BODY_HEIGHT)) {
            xy(PADDING, BODY_Y)
            wordWrap = true
        }

    internal val readMoreText: Text =
        content.text("", textSize = TEXT_SIZE, color = HudStyle.SELECTION) {
            xy(PADDING, READ_MORE_Y)
            onClick { delegate?.consentReadMoreSelected() }
        }

    private val buttonWidth = (PANEL_WIDTH - 2 * PADDING - BUTTON_SPACING) / 2
    private val buttonY = PANEL_HEIGHT - PADDING - UIButton.DEFAULT_SIZE.height

    internal val declineButton: GameButton =
        content.gameButton("", Size(buttonWidth, UIButton.DEFAULT_SIZE.height)) {
            xy(PADDING, buttonY)
            onClick { delegate?.consentDeclined() }
        }

    internal val acceptButton: GameButton =
        content.gameButton("", Size(buttonWidth, UIButton.DEFAULT_SIZE.height)) {
            xy(PADDING + buttonWidth + BUTTON_SPACING, buttonY)
            onClick { delegate?.consentAccepted() }
        }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display() {
        titleText.text = messages[UiMessage.CONSENT_TITLE]
        bodyText.text = RichTextData(messages[UiMessage.CONSENT_BODY], font = DefaultTtfFontAsBitmap, textSize = TEXT_SIZE, color = HudStyle.MUTED)
        readMoreText.text = messages[UiMessage.CONSENT_READ_MORE]
        declineButton.text = messages[UiMessage.CONSENT_DECLINE]
        acceptButton.text = messages[UiMessage.CONSENT_ACCEPT]
        visible = true
    }
}
