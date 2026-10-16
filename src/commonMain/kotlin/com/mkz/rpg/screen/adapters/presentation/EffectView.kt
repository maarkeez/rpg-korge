package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.blink
import korlibs.korge.animate.Animator
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class EffectView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIContainer(size) {
    companion object {
        const val EFFECT = "EFFECT"
    }

    internal var blinkAnimator: Animator? = null

    /** Displays the effect icon. A new effect, e.g.: one an ability cast would apply, blinks. */
    fun displayEffect(
        effectId: String,
        isNew: Boolean = false,
    ) {
        val container = this
        findViewByName(EFFECT)?.removeFromParent()
        val effectBitMap = resourcesLoader.bitmap("effect/$effectId.png")
        image(effectBitMap) {
            name = EFFECT
            smoothing = false
            scale = 1.0
            size = container.size
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
