package com.mkz.rpg.screen.adapters.presentation

fun EffectsView.effectSlotCount(): Int = effectViews.size

fun EffectsView.displayedEffectCount(): Int = effectViews.count { effectView -> effectView.isEffectIconDisplayed() }

fun EffectsView.blinkingEffects(): List<Boolean> = effectViews.filter { effectView -> effectView.isEffectIconDisplayed() }.map { effectView -> effectView.isBlinking() }

fun EffectsView.isOverflowDisplayed(): Boolean = overflowLabel.visible
