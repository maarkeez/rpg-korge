package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiText
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size

/** "Move" and "Cast" labels with one pip per action: filled while available, hollow once spent. */
class TurnPipsView(
    size: Size,
) : UIContainer(size) {
    companion object {
        const val PIP_FILLED = "PIP_FILLED"
        const val PIP_HOLLOW = "PIP_HOLLOW"
        private const val PIP_SIZE = 9
        private const val PIP_GAP = 3
        private const val LABEL_WIDTH = 40
        private const val GROUP_GAP = 14
    }

    val filledMovePips: Int get() = count(group = "MOVE", PIP_FILLED)
    val hollowMovePips: Int get() = count(group = "MOVE", PIP_HOLLOW)
    val filledCastPips: Int get() = count(group = "CAST", PIP_FILLED)
    val hollowCastPips: Int get() = count(group = "CAST", PIP_HOLLOW)

    fun display(
        remainingSteps: Int,
        maximumSteps: Int,
        remainingCasts: Int,
        maximumCasts: Int,
    ) {
        removeChildren()
        var x = drawGroup("MOVE", "Move", 0, remainingSteps, maxOf(maximumSteps, remainingSteps), UiPalette.move)
        x += GROUP_GAP
        drawGroup("CAST", "Cast", x, remainingCasts, maxOf(maximumCasts, remainingCasts), UiPalette.castValid)
    }

    private fun drawGroup(
        group: String,
        label: String,
        startX: Int,
        remaining: Int,
        maximum: Int,
        color: RGBA,
    ): Int {
        uiText(label, size = Size(LABEL_WIDTH, height)) {
            this.x = startX.toDouble()
            this.styles.textAlignment = TextAlignment.MIDDLE_LEFT
            this.styles.textColor = UiPalette.textPrimary
        }
        var x = startX + LABEL_WIDTH
        val top = (height - PIP_SIZE) / 2
        repeat(maximum) { index ->
            val filled = index < remaining
            addChild(
                pip(group, filled, color).also {
                    it.x = x.toDouble()
                    it.y = top
                },
            )
            x += PIP_SIZE + PIP_GAP
        }
        return x
    }

    /** Filled pip is a solid square; a hollow pip is only its 1 pt outline over the panel. */
    private fun pip(
        group: String,
        filled: Boolean,
        color: RGBA,
    ): UIContainer =
        UIContainer(Size(PIP_SIZE, PIP_SIZE)).also { container ->
            container.name = "$group:${if (filled) PIP_FILLED else PIP_HOLLOW}"
            val outline = SolidRect(Size(PIP_SIZE, PIP_SIZE), color)
            container.addChild(outline)
            if (!filled) {
                container.addChild(
                    SolidRect(Size(PIP_SIZE - 2, PIP_SIZE - 2), UiPalette.panel).also {
                        it.x = 1.0
                        it.y = 1.0
                    },
                )
            }
        }

    private fun count(
        group: String,
        kind: String,
    ): Int = children.count { it.name == "$group:$kind" }
}
