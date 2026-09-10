package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.roundRect
import korlibs.korge.view.setText
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlin.math.max

open class PreviewBarView(
    size: Size,
    filledColor: RGBA,
    previewColor: RGBA,
) : UIContainer(size) {
    private val background =
        roundRect(size, radius = RectCorners(3)) {
            this.color = Colors.DIMGRAY
        }

    private val filled =
        roundRect(size, radius = RectCorners(3)) {
            this.color = filledColor
            this.height = size.height
        }

    private val preview =
        roundRect(size, radius = RectCorners(3)) {
            this.color = previewColor
            this.height = size.height
        }

    private val label =
        text(
            text = "30 / 500",
            textSize = 14.0,
            color = Colors.WHITE,
        ) {
            centerOn(background)
        }

    init {
        addChild(background)
        addChild(preview)
        addChild(filled)
        addChild(label)
    }

    fun display(
        remainingBefore: Int,
        remainingAfter: Int,
        maximum: Int,
    ) {
        val remainingAfter = max(0, remainingAfter)
        val filledPercentage = (remainingAfter.toDouble() / maximum.toDouble())
        val previewPercentage = (remainingBefore.toDouble() / maximum.toDouble())
        this.filled.width = this.size.width * filledPercentage
        this.preview.width = this.size.width * previewPercentage
        this.label.setText("$remainingAfter / $maximum")
        visible = true
    }

    fun hide() {
        visible = false
    }
}
