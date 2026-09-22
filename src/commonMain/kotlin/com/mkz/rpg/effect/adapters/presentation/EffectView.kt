package com.mkz.rpg.effect.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

class EffectView(
    size: Size,
) : UIContainer(size) {
    companion object {
        const val EFFECT = "EFFECT"
    }

    private lateinit var effectBitmaps: Map<String, Bitmap>

    suspend fun loadAssets() {
        effectBitmaps =
            buildMap {
                put("venom-damage", resourcesVfs["effect/venom-damage.png"].readBitmap())
                put("venom-on-death", resourcesVfs["effect/venom-on-death.png"].readBitmap())
            }
    }

    fun displayEffect(effectId: String) {
        val container = this
        findViewByName(EFFECT)?.removeFromParent()
        val effectBitMap = effectBitmaps[effectId] ?: throw IllegalStateException("Effect $effectId bitmap not found")
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
