package com.mkz.tessavar.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.geom.degrees
import kotlin.math.max
import kotlin.math.min

open class PreviewBarView(
    size: Size,
    private val filledColor: RGBA,
    private val previewColor: RGBA,
    private val gainColor: RGBA = GAIN,
    private val showValue: Boolean = true,
) : UIContainer(size) {
    companion object {
        const val BAR_HEIGHT = 12.0
        const val TEXT_SIZE = 10.0
        private const val VALUE_SPACING = 6.0
        private const val ARROW_LENGTH = 8.0
        private const val ARROW_HEAD = 4.5

        val GAIN = RGBA(0x5A, 0xD2, 0x5A)
    }

    internal var track: RoundRect? = null
    internal var filled: RoundRect? = null
    internal var preview: RoundRect? = null
    internal val value: Container = container()
    internal var beforeText: Text? = null
    internal var afterText: Text? = null

    val barWidth: Double get() = track?.width ?: width

    internal var filledRatio: Double = 1.0
    internal var previewRatio: Double = 1.0

    fun display(
        remainingBefore: Int,
        remainingAfter: Int,
        maximum: Int,
    ) {
        val remainingAfter = max(0, remainingAfter)
        track?.removeFromParent()
        filled?.removeFromParent()
        preview?.removeFromParent()
        displayValue(remainingBefore, remainingAfter)
        val barWidth = width - if (showValue) value.width + VALUE_SPACING else 0.0
        val barHeight = min(height, BAR_HEIGHT)
        val barY = (height - barHeight) / 2
        val radius = barHeight / 2

        fun ratio(value: Int) = if (maximum > 0) value.coerceIn(0, maximum).toDouble() / maximum else 0.0
        track = roundRect(Size(barWidth, barHeight), RectCorners(radius), fill = BarView.TRACK, stroke = BarView.TRACK_STROKE, strokeThickness = 1.0) { xy(0.0, barY) }
        filledRatio = ratio(min(remainingBefore, remainingAfter))
        previewRatio = ratio(max(remainingBefore, remainingAfter))
        val previewWidth = barWidth * previewRatio
        val filledWidth = barWidth * filledRatio
        preview =
            roundRect(Size(previewWidth, barHeight), RectCorners(min(radius, previewWidth / 2)), fill = if (remainingAfter > remainingBefore) gainColor else previewColor) {
                xy(0.0, barY)
                visible = previewWidth > 0 && remainingAfter != remainingBefore
            }
        filled =
            roundRect(Size(filledWidth, barHeight), RectCorners(min(radius, filledWidth / 2)), fill = filledColor) {
                xy(0.0, barY)
                visible = filledWidth > 0
            }
        addChild(value)
        value.xy(width - value.width, (height - TEXT_SIZE) / 2 - 1)
        visible = true
    }

    fun hide() {
        visible = false
    }

    private fun displayValue(
        before: Int,
        after: Int,
    ) {
        value.removeChildren()
        value.visible = showValue
        val color = Colors.WHITE
        val from = value.text("$before", textSize = TEXT_SIZE, color = color.withAd(0.7))
        val arrowX = from.width + 4
        val middle = TEXT_SIZE * 0.62
        value.solidRect(Size(ARROW_LENGTH, 1.5), color.withAd(0.7)) { xy(arrowX, middle - 0.75) }
        value.solidRect(Size(ARROW_HEAD, ARROW_HEAD), color.withAd(0.7)) {
            rotation = 45.degrees
            xy(arrowX + ARROW_LENGTH, middle - 3.2)
        }
        val to = value.text("$after", textSize = TEXT_SIZE, color = color) { xy(arrowX + ARROW_LENGTH + 7, 0.0) }
        beforeText = from
        afterText = to
    }
}
