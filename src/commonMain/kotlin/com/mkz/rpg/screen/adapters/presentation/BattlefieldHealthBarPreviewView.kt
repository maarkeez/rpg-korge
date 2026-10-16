package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.ColorPalette
import com.mkz.rpg.shared.adapters.presentation.blink
import korlibs.korge.animate.Animator
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.View
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

class BattlefieldHealthBarPreviewView(
    size: Size,
) : UIContainer(size) {
    // The blinks of the previous preview are cancelled, as removing their views does not stop them.
    internal val blinkAnimators = mutableListOf<Animator>()

    fun display(previewOption: PreviewOption) {
        blinkAnimators.forEach(Animator::cancel)
        blinkAnimators.clear()
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
        blinkAnimators += view.blink(firstAlpha = 0.5, secondAlpha = 0.0)
    }

    private fun blinkFull(view: View) {
        blinkAnimators += view.blink(firstAlpha = 0.5, secondAlpha = 1.0)
    }

    enum class PreviewOption {
        EMPTY,
        ONE_OUT_OF_THREE,
        TWO_OUT_OF_THREE,
        THREE_OUT_OF_THREE,
        FULL,
    }
}
