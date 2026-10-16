package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animator
import korlibs.korge.animate.block
import korlibs.korge.animate.scaleTo
import korlibs.korge.animate.wait
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size
import korlibs.time.milliseconds
import korlibs.time.seconds

/** The map builder header: its title, and a subtitle with the map size, which messages replace for a while. */
open class MapBuilderInfoView : UIContainer(Size(width = 390, height = 33)) {
    companion object {
        const val TITLE = "Map Builder"

        // FamiCube palette colours of the messages.
        val ERROR_COLOR = RGBA(0xE0, 0x3C, 0x28)
        val INFO_COLOR = RGBA(0x00, 0x84, 0xFF)

        // A message grows to this scale and shrinks back, like the battle turns, and stays until the size comes back.
        const val POP_SCALE = 1.3
        val POP_DURATION = 400.milliseconds
        val MESSAGE_DURATION = 2.seconds
    }

    internal val title =
        uiText(text = TITLE, size = Size(width = width, height = 18)) {
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
            styles.textSize = 18.0
        }
    internal val subtitle =
        uiText(text = "", size = Size(width = width, height = 15)) {
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
            styles.textSize = 15.0
        }

    init {
        uiVerticalStack {
            addChild(title)
            addChild(subtitle)
        }
    }

    private var sizeText = ""

    // The message on display, until the size comes back.
    internal var messageAnimator: Animator? = null

    /** Displays the map size, e.g.: "16 x 16". A message on display keeps it until the message goes away. */
    open fun displaySize(
        rows: Int,
        columns: Int,
    ) {
        sizeText = "$rows x $columns"
        if (messageAnimator == null) displaySizeText()
    }

    /** Pops the error in the subtitle for a while, e.g.: why a tile can not be painted. */
    open fun displayError(message: String) = displayMessage(message, ERROR_COLOR)

    /** Pops the information in the subtitle for a while, e.g.: the map was copied. */
    open fun displayInfo(message: String) = displayMessage(message, INFO_COLOR)

    private fun displayMessage(
        message: String,
        color: RGBA,
    ) {
        // A new message replaces the one on display, and its own time starts again.
        messageAnimator?.cancel()
        subtitle.scale = 1.0
        subtitle.text = message
        subtitle.styles.textColor = color
        messageAnimator =
            animator {
                scaleTo(subtitle, POP_SCALE, time = POP_DURATION / 2)
                scaleTo(subtitle, 1.0, time = POP_DURATION / 2)
                wait(MESSAGE_DURATION)
                block {
                    messageAnimator = null
                    displaySizeText()
                }
            }
    }

    private fun displaySizeText() {
        subtitle.text = sizeText
        subtitle.styles.textColor = Colors.WHITE
    }
}
