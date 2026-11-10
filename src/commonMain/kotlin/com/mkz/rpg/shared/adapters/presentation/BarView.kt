package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlin.math.min

open class BarView(
    size: Size,
    private val color: RGBA,
    private val showValue: Boolean = true,
) : UIContainer(size) {
    companion object {
        const val BAR_HEIGHT = 12.0
        const val TEXT_SIZE = 12.0
        private const val VALUE_SPACING = 6.0

        val TRACK = RGBA(0x0E, 0x0E, 0x18)
        val TRACK_STROKE = RGBA(0x36, 0x36, 0x50)
    }

    internal var track: RoundRect? = null
    internal var fill: RoundRect? = null
    internal val label: Text = text(text = "", textSize = TEXT_SIZE, color = Colors.WHITE)

    internal var ratio: Double = 1.0

    init {
        visible = false
    }

    fun display(
        remaining: Int,
        maximum: Int,
    ) {
        ratio = if (maximum > 0) remaining.coerceIn(0, maximum).toDouble() / maximum else 0.0
        track?.removeFromParent()
        fill?.removeFromParent()
        label.text = "$remaining/$maximum"
        label.visible = showValue
        val widestValueWidth = if (showValue) Text("$maximum/$maximum", textSize = TEXT_SIZE).width + VALUE_SPACING else 0.0
        val barWidth = width - widestValueWidth
        val barHeight = min(height, BAR_HEIGHT)
        val barY = (height - barHeight) / 2
        val radius = barHeight / 2
        track =
            roundRect(Size(barWidth, barHeight), RectCorners(radius), fill = TRACK, stroke = TRACK_STROKE, strokeThickness = 1.0) {
                xy(0.0, barY)
            }
        val fillWidth = barWidth * ratio
        fill =
            roundRect(Size(fillWidth, barHeight), RectCorners(min(radius, fillWidth / 2)), fill = color) {
                xy(0.0, barY)
                visible = fillWidth > 0
            }
        label.xy(width - label.width, (height - label.height) / 2)
        addChild(label)
        visible = true
    }

    fun hide() {
        visible = false
    }
}
