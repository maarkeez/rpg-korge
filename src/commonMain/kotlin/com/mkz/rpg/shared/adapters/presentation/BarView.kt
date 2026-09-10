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
    private val progressBar: UIProgressBar =
        uiProgressBar(size = size, current = 100f, maximum = 100f).also { progressBar ->
            progressBar.styles.uiSelectedColor = color
            progressBar.styles.uiBackgroundColor = Colors.DIMGREY
        }
    private val label =
        text(
            text = "",
            textSize = 14.0,
            color = Colors.WHITE,
        )

    init {
        addChild(progressBar)
        addChild(label)
    }

    fun display(
        remaining: Int,
        maximum: Int,
    ) {
        val healthPercentage = (remaining.toDouble() / maximum.toDouble()) * 100
        label.setText("$remaining / $maximum")
        progressBar.current = healthPercentage
        label.centerXOn(progressBar)
        label.y = (progressBar.height - label.height) / 2 + 1
        visible = true
    }

    fun hide() {
        visible = false
    }
}
