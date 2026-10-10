package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class EffectsView(
    resourcesLoader: BattleSceneResourcesLoader,
) : Container() {
    private lateinit var effectViews: Array<EffectView>

    private val effectViewsLayout =
        uiHorizontalStack(padding = 2.0) {
            val effectViewSize = Size(width = 16, height = 16)
            effectViews = Array(10) { EffectView(effectViewSize, resourcesLoader) }
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
