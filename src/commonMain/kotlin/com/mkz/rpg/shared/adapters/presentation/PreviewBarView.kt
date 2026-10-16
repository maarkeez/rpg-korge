package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.style.styles
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIProgressBar
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiProgressBar
import korlibs.korge.ui.uiSelectedColor
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.setText
import korlibs.korge.view.text
import korlibs.math.geom.Size
import kotlin.math.max

/**
 * A bar filled up to the remaining value after a change, over the bar filled up to the value before it. Drawn with
 * progress bars, as they are cut by the clip containers (e.g.: when scrolled), unlike the round rect views.
 */
open class PreviewBarView(
    size: Size,
    filledColor: RGBA,
    previewColor: RGBA,
) : UIContainer(size) {
    internal val preview: UIProgressBar =
        uiProgressBar(size = size, current = 100f, maximum = 100f).also { progressBar ->
            progressBar.styles.uiSelectedColor = previewColor
            progressBar.styles.uiBackgroundColor = Colors.DIMGRAY
        }

    internal val filled: UIProgressBar =
        uiProgressBar(size = size, current = 100f, maximum = 100f).also { progressBar ->
            progressBar.styles.uiSelectedColor = filledColor
            progressBar.styles.uiBackgroundColor = Colors.TRANSPARENT
        }

    internal val label =
        text(
            text = "30 / 500",
            textSize = 14.0,
            color = Colors.WHITE,
        ) {
            centerOn(preview)
        }

    fun display(
        remainingBefore: Int,
        remainingAfter: Int,
        maximum: Int,
    ) {
        val remainingAfter = max(0, remainingAfter)
        this.filled.maximum = maximum.toDouble()
        this.filled.current = remainingAfter.toDouble()
        this.preview.maximum = maximum.toDouble()
        this.preview.current = remainingBefore.toDouble()
        this.label.setText("$remainingAfter / $maximum")
        visible = true
    }

    fun hide() {
        visible = false
    }
}
