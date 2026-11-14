package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.ColorPalette
import com.mkz.tessavar.shared.adapters.presentation.blink
import korlibs.korge.animate.Animator
import korlibs.korge.style.styles
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiProgressBar
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.View
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

class BattlefieldHealthBarPreviewView(
    size: Size,
) : UIContainer(size) {
    internal val blinkAnimators = mutableListOf<Animator>()

    private val heldAlphas = mutableMapOf<View, Double>()

    internal val blinkingThirds: List<View>
        get() = heldAlphas.keys.toList()

    internal var previewOption: PreviewOption? = null
        private set

    fun display(previewOption: PreviewOption) {
        stop()
        heldAlphas.clear()
        removeChildren()
        this.previewOption = previewOption

        val paddingUnit = 1
        val previewUnit = 3
        val totalUnits = previewUnit + paddingUnit + previewUnit + paddingUnit + previewUnit
        val paddingWidth = paddingUnit * (size.width * 0.8 / totalUnits)
        val previewWidth = previewUnit * (size.width * 0.8 / totalUnits)
        val paddingHeight = size.height * 0.3
        val previewHeight = size.height * 0.4
        val totalSize = size
        val totalWidth = totalSize.width
        uiProgressBar(size = totalSize, current = 0.0, maximum = 1.0).also { progressBar ->
            progressBar.styles.uiBackgroundColor = ColorPalette.SystemGray5.INCREASED_CONTRAST
        }

        uiHorizontalStack {
            uiSpacing(size = Size(width = totalWidth * 0.1, height = 0.0))
            uiVerticalStack {
                uiSpacing(size = Size(width = 0.0, height = paddingHeight))
                uiHorizontalStack(padding = paddingWidth) {
                    val firstPreview = solidRect(Size(width = previewWidth, height = previewHeight), color = ColorPalette.RED)
                    val secondPreview = solidRect(Size(width = previewWidth, height = previewHeight), color = ColorPalette.RED)
                    val thirdPreview = solidRect(Size(width = previewWidth, height = previewHeight), color = ColorPalette.RED)
                    when (previewOption) {
                        PreviewOption.EMPTY -> {
                            blinkEmpty(firstPreview)
                            secondPreview.alpha = 0.0
                            thirdPreview.alpha = 0.0
                        }
                        PreviewOption.ONE_OUT_OF_THREE -> {
                            blinkFull(firstPreview)
                            secondPreview.alpha = 0.0
                            thirdPreview.alpha = 0.0
                        }
                        PreviewOption.TWO_OUT_OF_THREE -> {
                            blinkFull(secondPreview)
                            thirdPreview.alpha = 0.0
                        }
                        PreviewOption.THREE_OUT_OF_THREE -> {
                            blinkFull(thirdPreview)
                        }
                        PreviewOption.FULL -> Unit
                    }
                }
            }
        }
    }

    fun stop() {
        blinkAnimators.forEach(Animator::cancel)
        blinkAnimators.clear()
    }

    fun hold() {
        stop()
        heldAlphas.forEach { (third, alpha) -> third.alpha = alpha }
    }

    private fun blinkEmpty(view: View) {
        blinkAnimators += view.blink(firstAlpha = 0.5, secondAlpha = 0.0)
        heldAlphas[view] = 0.0
    }

    private fun blinkFull(view: View) {
        blinkAnimators += view.blink(firstAlpha = 0.5, secondAlpha = 1.0)
        heldAlphas[view] = 1.0
    }

    enum class PreviewOption {
        EMPTY,
        ONE_OUT_OF_THREE,
        TWO_OUT_OF_THREE,
        THREE_OUT_OF_THREE,
        FULL,
        ;

        companion object {
            fun of(
                remaining: Int,
                maximum: Int,
            ): PreviewOption =
                when {
                    remaining <= 0 -> EMPTY
                    remaining * 3 <= maximum -> ONE_OUT_OF_THREE
                    remaining * 3 <= maximum * 2 -> TWO_OUT_OF_THREE
                    remaining < maximum -> THREE_OUT_OF_THREE
                    else -> FULL
                }
        }
    }
}
