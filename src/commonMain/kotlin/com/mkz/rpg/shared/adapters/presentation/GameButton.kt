package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.addTo
import korlibs.math.geom.Point
import korlibs.math.geom.Size

/**
 * The button of the game: grey by default, with a light border and a white label. A disabled button is still displayed,
 * but faded, its background dark, its label grey and its border dim, so the player tells at a glance that it can not be
 * tapped: KorGE's own disabled grey is too close to the grey of the game to tell apart.
 */
class GameButton(
    size: Size,
    label: String,
    color: RGBA = GREY,
    overColor: RGBA = GREY_OVER,
) : UIButton(size, label) {
    companion object {
        val GREY: RGBA = Colors.DIMGRAY
        val GREY_OVER = RGBA(85, 85, 85)
        val LABEL: RGBA = Colors.WHITE
        val BORDER: RGBA = Colors.LIGHTGRAY
        val DISABLED = RGBA(58, 58, 58)
        val DISABLED_LABEL = RGBA(138, 138, 138)
        val DISABLED_BORDER = RGBA(85, 85, 85)
    }

    init {
        bgColorOut = color
        bgColorOver = overColor
        bgColorDisabled = DISABLED
        background.borderColor = BORDER
    }

    // KorGE only changes the background of a disabled button: the label and the border fade with it.
    override fun updatedUIButton(
        down: Boolean?,
        over: Boolean?,
        enable: Boolean?,
        pos: Point,
        immediate: Boolean,
    ) {
        super.updatedUIButton(down, over, enable, pos, immediate)
        val isDisabled = enable == false || !enabled
        textColor = if (isDisabled) DISABLED_LABEL else LABEL
        background.borderColor = if (isDisabled) DISABLED_BORDER else BORDER
    }
}

fun Container.gameButton(
    label: String,
    size: Size,
    color: RGBA = GameButton.GREY,
    overColor: RGBA = GameButton.GREY_OVER,
    block: GameButton.() -> Unit = {},
): GameButton = GameButton(size, label, color, overColor).addTo(this).apply(block)
