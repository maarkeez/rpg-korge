package com.mkz.rpg.screen.adapters.presentation

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

    fun displayEffect(effectId: String) {
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
        visible = true
    }

    fun hide() {
        visible = false
    }
}
