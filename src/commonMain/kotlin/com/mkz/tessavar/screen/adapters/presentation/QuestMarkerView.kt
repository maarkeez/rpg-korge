package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.exclamationGlyph
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.taskGlyph
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.geom.degrees

class QuestMarkerView(
    sign: Sign,
) : Container() {
    enum class Sign { QUEST_TO_START, TALK_TASK }

    private companion object {
        const val DISC_X = 20.0
        const val DISC_Y = -14.0
        const val DISC_SIZE = 24.0
        const val DISC_STROKE = 1.5
        const val POINTER_SIZE = 10.0
        const val POINTER_X = 32.0
        const val POINTER_Y = 3.0
        const val SIGN_SIZE = 14.0
        const val SIGN_INSET = (DISC_SIZE - SIGN_SIZE) / 2
    }

    internal val color = if (sign == Sign.QUEST_TO_START) HudStyle.SELECTION else HudStyle.ALLY

    init {
        solidRect(Size(POINTER_SIZE, POINTER_SIZE), color) {
            rotation = 45.degrees
            xy(POINTER_X, POINTER_Y)
        }
        roundRect(Size(DISC_SIZE, DISC_SIZE), RectCorners(DISC_SIZE / 2), fill = color, stroke = Colors.BLACK, strokeThickness = DISC_STROKE) { xy(DISC_X, DISC_Y) }
        when (sign) {
            Sign.QUEST_TO_START -> exclamationGlyph(DISC_X + SIGN_INSET, DISC_Y + SIGN_INSET, SIGN_SIZE, Colors.BLACK)
            Sign.TALK_TASK -> taskGlyph(QuestInfo.Kind.TALK, DISC_X + SIGN_INSET, DISC_Y + SIGN_INSET, SIGN_SIZE, Colors.BLACK)
        }
    }
}
