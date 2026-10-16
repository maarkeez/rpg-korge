package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.text
import korlibs.math.geom.Size

class EffectsView(
    resourcesLoader: BattleSceneResourcesLoader,
) : Container() {
    internal lateinit var effectViews: Array<EffectView>

    // Displayed after the effect icons when there are more effects than icons.
    internal lateinit var overflowLabel: Text

    private val effectViewsLayout =
        uiHorizontalStack(padding = 2.0) {
            val effectViewSize = Size(width = 16, height = 16)
            effectViews = Array(10) { EffectView(effectViewSize, resourcesLoader) }
            effectViews.forEach(::addChild)
            overflowLabel = text("+", textSize = 14.0, color = Colors.WHITE)
        }

    init {
        visible = false
        addChild(effectViewsLayout)
    }

    /** Displays the [effectIds] of a battle unit, followed by the [newEffectIds] it would receive, which blink. */
    fun display(
        effectIds: Collection<String>,
        newEffectIds: Collection<String> = emptyList(),
    ) {
        effectViews.forEach { effectView -> effectView.hide() }
        val effects = effectIds.map { effectId -> effectId to false } + newEffectIds.map { effectId -> effectId to true }
        effects.take(effectViews.size).forEachIndexed { index, (effectId, isNew) -> effectViews[index].displayEffect(effectId, isNew) }
        overflowLabel.visible = effects.size > effectViews.size
        visible = true
    }

    fun hide() {
        visible = false
    }
}
