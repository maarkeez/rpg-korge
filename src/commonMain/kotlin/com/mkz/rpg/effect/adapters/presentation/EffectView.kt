package com.mkz.rpg.effect.adapters.presentation

import com.mkz.rpg.screen.SpriteRegistry
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class EffectView(
    size: Size,
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : UIContainer(size) {
    companion object {
        const val EFFECT = "EFFECT"
    }

    suspend fun loadAssets() {
        sprites.load()
    }

    fun displayEffect(effectId: String) {
        val container = this
        findViewByName(EFFECT)?.removeFromParent()
        image(sprites.effect(effectId)) {
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
