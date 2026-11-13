package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.gameButton
import korlibs.image.color.Colors
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

class PrivacyNoticeView(
    private val messages: Messages,
) : Container() {
    interface Delegate {
        fun privacyNoticeBackSelected()
    }

    companion object {
        private const val SCREEN_WIDTH = 390.0
        private const val SCREEN_HEIGHT = 844.0
        private const val PANEL_WIDTH = 350.0
        private const val PANEL_HEIGHT = 640.0
        private const val PANEL_RADIUS = 12.0
        private const val PADDING = 20.0
        private const val TITLE_SIZE = 20.0
        private const val BODY_Y = 56.0
        private const val TEXT_SIZE = 15.0
    }

    private var delegate: Delegate? = null

    init {
        solidRect(SCREEN_WIDTH, SCREEN_HEIGHT, ConsentView.DIM).onClick { }
    }

    private val content =
        container {
            xy((SCREEN_WIDTH - PANEL_WIDTH) / 2, (SCREEN_HEIGHT - PANEL_HEIGHT) / 2)
            panel(Size(PANEL_WIDTH, PANEL_HEIGHT), radius = PANEL_RADIUS, stroke = HudStyle.SELECTION)
        }

    private val buttonY = PANEL_HEIGHT - PADDING - UIButton.DEFAULT_SIZE.height

    internal val titleText: Text = content.text("", textSize = TITLE_SIZE, color = Colors.WHITE) { xy(PADDING, PADDING) }

    internal val bodyText: TextBlock =
        content.textBlock(RichTextData(""), align = TextAlignment.TOP_LEFT, size = Size(PANEL_WIDTH - 2 * PADDING, buttonY - BODY_Y - PADDING)) {
            xy(PADDING, BODY_Y)
            wordWrap = true
        }

    internal val backButton: GameButton =
        content.gameButton("", Size(PANEL_WIDTH - 2 * PADDING, UIButton.DEFAULT_SIZE.height)) {
            xy(PADDING, buttonY)
            onClick { delegate?.privacyNoticeBackSelected() }
        }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display() {
        titleText.text = messages[UiMessage.PRIVACY_NOTICE_TITLE]
        bodyText.text = RichTextData(messages[UiMessage.PRIVACY_NOTICE_BODY], font = DefaultTtfFontAsBitmap, textSize = TEXT_SIZE, color = HudStyle.MUTED)
        backButton.text = messages[UiMessage.PRIVACY_NOTICE_BACK]
        visible = true
    }
}
