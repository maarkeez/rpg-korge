package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.font.DefaultTtfFontAsBitmap
import korlibs.image.text.RichTextData
import korlibs.image.text.TextAlignment
import korlibs.korge.view.Container
import korlibs.korge.view.TextBlock
import korlibs.korge.view.position
import korlibs.korge.view.textBlock
import korlibs.math.geom.Size

/**
 * A question displayed in place of the pad before an action that can not be undone, e.g.: quitting to the menu, which
 * loses the progress since the last save. The message, centred and wrapping when long. Its choices are in the row of the
 * call to action, displayed by the presenter that asks.
 */
open class ConfirmDialogView : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val PADDING = 20.0
        private const val MESSAGE_Y = 95.0
        private const val MESSAGE_HEIGHT = 110.0
        private const val MESSAGE_TEXT_SIZE = 18.0
    }

    internal val messageText: TextBlock =
        textBlock(RichTextData(""), align = TextAlignment.MIDDLE_CENTER, size = Size(WIDTH - 2 * PADDING, MESSAGE_HEIGHT)) {
            position(PADDING, MESSAGE_Y)
            wordWrap = true
        }

    /** Asks the [message], e.g.: Quit to the menu? */
    open fun display(message: String) {
        messageText.text = RichTextData(message, font = DefaultTtfFontAsBitmap, textSize = MESSAGE_TEXT_SIZE, color = Colors.WHITE)
    }
}
