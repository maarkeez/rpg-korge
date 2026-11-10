package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import com.mkz.rpg.shared.adapters.presentation.blink
import korlibs.korge.animate.Animator
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class EffectView(
    size: Size,
    private val resourcesLoader: GameSceneResourcesLoader,
) : UIContainer(size) {
    companion object {
        const val EFFECT = "EFFECT"
        const val CHIP = "CHIP"

        private const val CHIP_RADIUS = 5.0
    }

    internal var blinkAnimator: Animator? = null

    fun displayEffect(
        effectId: String,
        isNew: Boolean = false,
    ) {
        val container = this
        findViewByName(CHIP)?.removeFromParent()
        findViewByName(EFFECT)?.removeFromParent()
        panel(size, fill = HudStyle.PANEL_RAISED, radius = CHIP_RADIUS, stroke = if (isNew) HudStyle.SELECTION else HudStyle.STROKE).name = CHIP
        val effectBitMap = resourcesLoader.bitmap("effect/$effectId.png")
        image(effectBitMap) {
            name = EFFECT
            smoothing = false
            scale = 1.0
            centerOn(container)
        }
        stopBlinking()
        if (isNew) blinkAnimator = blink(firstAlpha = 0.5, secondAlpha = 1.0)
        visible = true
    }

    fun hide() {
        stopBlinking()
        visible = false
    }

    private fun stopBlinking() {
        blinkAnimator?.cancel()
        blinkAnimator = null
        alpha = 1.0
    }
}
