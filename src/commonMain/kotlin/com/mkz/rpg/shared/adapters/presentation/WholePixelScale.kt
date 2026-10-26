package com.mkz.rpg.shared.adapters.presentation

import korlibs.math.geom.ScaleMode
import kotlin.math.floor
import kotlin.math.min

/**
 * Scales the stage to the window by the largest whole number that fits it, so every pixel of the stage, and of its pixel
 * art, is drawn with the same number of pixels of the window. The window is measured in its device pixels, e.g.: a
 * 390x844 window on a 2x screen holds a 390x844 stage at 2x. When even 1x does not fit, the stage is scaled down to fit.
 */
val WholePixelScale: ScaleMode =
    ScaleMode("WHOLE_PIXEL") { stage, window ->
        val fittingScale = min(window.width / stage.width, window.height / stage.height)
        stage * if (fittingScale < 1) fittingScale else floor(fittingScale)
    }
