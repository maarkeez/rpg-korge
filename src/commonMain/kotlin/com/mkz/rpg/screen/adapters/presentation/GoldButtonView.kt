package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.GameButton
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.circle
import korlibs.korge.view.container
import korlibs.korge.view.roundRect
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

/**
 * The gold of a party member on the title line of the bag while exploring, looking like the buttons next to it, so the
 * player expects to tap it to give gold. A faded one can not give any, but can still be tapped, to read why.
 */
class GoldButtonView(
    private val onSelected: () -> Unit,
) : Container() {
    companion object {
        const val HEIGHT = 28.0
        private const val PADDING = 9.0
        private const val COIN = 14.0
        private const val SPACING = 5.0
        private const val RADIUS = 7.0
        private const val TEXT_SIZE = 15.0
        private const val FADED_COIN_ALPHA = 0.45
    }

    /** How wide it is drawn, to align it to the right. */
    var buttonWidth = 0.0
        private set

    internal var amountText = ""
        private set
    internal var isFaded = false
        private set

    init {
        onClick { onSelected() }
    }

    fun display(
        text: String,
        isFaded: Boolean,
    ) {
        amountText = text
        this.isFaded = isFaded
        removeChildren()
        val label = Text(text, textSize = TEXT_SIZE, color = if (isFaded) GameButton.DISABLED_LABEL else GameButton.LABEL)
        buttonWidth = PADDING * 2 + COIN + SPACING + label.width
        // The border is a shape of its colour with the fill inset over it: a stroke is drawn thinner and fainter than its thickness.
        roundRect(Size(buttonWidth, HEIGHT), RectCorners(RADIUS), fill = if (isFaded) GameButton.DISABLED_BORDER else GameButton.BORDER)
        roundRect(Size(buttonWidth - 2, HEIGHT - 2), RectCorners(RADIUS - 1), fill = if (isFaded) GameButton.DISABLED else GameButton.GREY) { xy(1.0, 1.0) }
        container {
            alpha = if (isFaded) FADED_COIN_ALPHA else 1.0
            circle(radius = COIN / 2, fill = HudStyle.SELECTION)
            circle(radius = COIN / 4, fill = HudStyle.COIN_MIDDLE) { xy(COIN / 4, COIN / 4) }
            xy(PADDING, (HEIGHT - COIN) / 2)
        }
        addChild(label)
        label.xy(PADDING + COIN + SPACING, (HEIGHT - label.height) / 2)
    }
}
