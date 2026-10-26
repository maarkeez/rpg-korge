package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.style.styles
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiText
import korlibs.korge.view.setText
import korlibs.math.geom.Size

/** Tells how many turns a summon has left before it vanishes, the current one included, e.g.: "Vanishes in 3 turns". */
class RemainingTurnsView(
    size: Size,
    private val texts: Texts,
) : UIContainer(size) {
    private companion object {
        // Smaller than the unit name, so it fits next to the remaining steps and casts.
        const val TEXT_SIZE = 12.0
    }

    internal val remainingTurnsLabel = uiText("", size = size) { styles.textSize = TEXT_SIZE }

    init {
        visible = false
    }

    fun display(remainingTurns: Int) {
        remainingTurnsLabel.setText(texts.plural(UiText.REMAINING_TURNS, remainingTurns, "turns" to remainingTurns))
        visible = true
    }

    fun hide() {
        visible = false
    }
}
