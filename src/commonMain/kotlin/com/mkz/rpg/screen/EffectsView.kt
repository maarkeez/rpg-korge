package com.mkz.rpg.screen

import com.mkz.rpg.effect.adapters.presentation.EffectView
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class EffectsView(
    sprites: SpriteRegistry = SpriteRegistry(),
) : Container() {
    private lateinit var effectViews: Array<EffectView>

    suspend fun loadAssets() {
        effectViews.forEach { effectView -> effectView.loadAssets() }
    }

    private val effectViewsLayout =
        uiHorizontalStack(padding = 2.0) {
            val effectViewSize = Size(width = 32, height = 32)
            effectViews =
                arrayOf(
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                    EffectView(effectViewSize, sprites),
                )
            effectViews.forEach(::addChild)
        }

    init {
        visible = false
        addChild(effectViewsLayout)
    }

    fun display(effectIds: Collection<String>) {
        effectViews.forEach { effectView -> effectView.hide() }
        // TODO: Display more than 10
        effectIds.take(10).forEachIndexed { index, effectId -> effectViews[index].displayEffect(effectId) }
        visible = true
    }

    fun hide() {
        visible = false
    }
}
