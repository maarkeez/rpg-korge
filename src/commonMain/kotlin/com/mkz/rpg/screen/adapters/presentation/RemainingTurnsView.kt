package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.math.geom.Size

/** Tells how many turns a summon has left before it vanishes, the current one included, in a pill, e.g.: "Vanishes in 3 turns". */
class RemainingTurnsView(
    size: Size,
    private val texts: Texts,
) : UIContainer(size) {
    private companion object {
        // Smaller than the unit name, so it fits after it.
        const val TEXT_SIZE = 10.0
    }

    internal val pill = PillView(color = HudStyle.MUTED, textSize = TEXT_SIZE)

    init {
        addChild(pill)
        visible = false
    }

    fun display(remainingTurns: Int) {
        pill.display(texts.plural(UiText.REMAINING_SUMMON_DURATION, remainingTurns, "turns" to remainingTurns))
        visible = true
    }

    fun hide() {
        visible = false
    }
}
