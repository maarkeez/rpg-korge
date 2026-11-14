package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.ui.UIContainer
import korlibs.math.geom.Size

class RemainingSummonDurationView(
    size: Size,
    private val messages: Messages,
) : UIContainer(size) {
    private companion object {
        const val TEXT_SIZE = 10.0
    }

    internal val pill = PillView(color = HudStyle.MUTED, textSize = TEXT_SIZE)

    init {
        addChild(pill)
        visible = false
    }

    fun display(remainingTurns: Int) {
        pill.display(messages.plural(UiMessage.REMAINING_SUMMON_DURATION, remainingTurns, "turns" to remainingTurns))
        visible = true
    }

    fun hide() {
        visible = false
    }
}
