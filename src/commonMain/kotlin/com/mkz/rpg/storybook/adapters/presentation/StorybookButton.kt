package com.mkz.rpg.storybook.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

/**
 * A button of the Storybook's own panels, e.g.: an option of an enum control, highlighted while it is selected or under
 * the mouse. It paints itself, as a `UIButton` tween would overwrite the color of its selection.
 */
class StorybookButton(
    label: String,
    size: Size,
    private val onTapped: () -> Unit,
) : Container() {
    companion object {
        const val HEIGHT = 28.0
        const val TEXT_SIZE = 15.0
        val COLOR = RGBA(0x33, 0x33, 0x33)
        val HOVER_COLOR = RGBA(0x44, 0x44, 0x44)

        /** As wide as its [label] needs, at least [minimumWidth]. */
        fun sizeFor(
            label: String,
            minimumWidth: Double = HEIGHT,
        ): Size = Size(maxOf(minimumWidth, label.length * TEXT_SIZE * 0.6 + 2 * TEXT_SIZE), HEIGHT)
    }

    internal val background: RoundRect = roundRect(size, RectCorners(5.0), fill = Colors.WHITE)
    internal val label: Text = text(label, textSize = TEXT_SIZE, color = Colors.WHITE).also { text -> text.centerOn(background) }
    private var isOver = false

    internal var isSelected = false
        set(value) {
            field = value
            paint()
        }

    init {
        paint()
        onClick { onTapped() }
        onOver {
            isOver = true
            paint()
        }
        onOut {
            isOver = false
            paint()
        }
    }

    private fun paint() {
        background.color =
            when {
                isSelected -> SidebarView.SELECTED_COLOR
                isOver -> HOVER_COLOR
                else -> COLOR
            }
    }
}
