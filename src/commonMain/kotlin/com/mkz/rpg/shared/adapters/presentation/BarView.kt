package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIProgressBar
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiProgressBar
import korlibs.korge.ui.uiSelectedColor
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.setText
import korlibs.korge.view.text
import korlibs.math.geom.Size

open class BarView(
    size: Size,
    color: RGBA,
) : UIContainer(size) {
    val progressBar: UIProgressBar =
        uiProgressBar(size = size, current = 100f, maximum = 100f).also { progressBar ->
            progressBar.styles.uiSelectedColor = color
            progressBar.styles.uiBackgroundColor = Colors.DIMGREY
        }
    val label =
        text(
            text = "",
            textSize = 14.0,
            color = Colors.WHITE,
        )

    init {
        addChild(progressBar)
        addChild(label)
        visible(false)
    }

    fun display(
        remaining: Int,
        maximum: Int,
    ) {
        val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
        label.setText("$remaining / $maximum")
        progressBar.current = percentage
        label.centerXOn(progressBar)
        label.y = (progressBar.height - label.height) / 2 + 1
        visible(true)
    }

    fun hide() {
        visible(false)
    }

    private fun visible(value: Boolean) {
        visible = value
        progressBar.visible = value
        label.visible = value
    }
}
