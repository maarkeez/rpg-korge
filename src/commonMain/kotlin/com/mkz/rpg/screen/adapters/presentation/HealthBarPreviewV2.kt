package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.ColorPalette
import korlibs.korge.animate.alpha
import korlibs.korge.animate.animator
import korlibs.korge.animate.wait
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.View
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.time.milliseconds

class HealthBarPreviewV2(
    size: Size,
) : UIContainer(size) {
    fun display(previewOption: PreviewOption) {
        removeChildren()

        val paddingUnit = 1
        val previewUnit = 3
        val totalUnits = previewUnit + paddingUnit + previewUnit + paddingUnit + previewUnit
        val paddingWidth = paddingUnit * (size.width * 0.8 / totalUnits)
        val previewWidth = previewUnit * (size.width * 0.8 / totalUnits)
        val paddingHeight = size.height * 0.3
        val previewHeight = size.height * 0.4
        val totalSize = size
        val totalWidth = totalSize.width
        roundRect(totalSize, radius = RectCorners(3), fill = ColorPalette.SystemGray5.INCREASED_CONTRAST)

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
                        PreviewOption.FULL -> {
                            // Do nothing
                        }
                    }
                }
            }
        }
    }

    private fun blinkEmpty(view: View) {
        animator(parallel = true, looped = true) {
            sequence(looped = true) {
                alpha(view, 0.5)
                wait(200.milliseconds)
                alpha(view, 0.0)
                wait(200.milliseconds)
            }
        }
    }

    private fun blinkFull(view: View) {
        animator(parallel = true, looped = true) {
            sequence(looped = true) {
                alpha(view, 0.5)
                wait(200.milliseconds)
                alpha(view, 1.0)
                wait(200.milliseconds)
            }
        }
    }

    enum class PreviewOption {
        EMPTY,
        ONE_OUT_OF_THREE,
        TWO_OUT_OF_THREE,
        THREE_OUT_OF_THREE,
        FULL,
    }
}
