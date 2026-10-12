package com.mkz.rpg.screen.adapters.presentation

// Page object of [EffectsView]. Use `EffectViewExtension` to inspect each effect.

fun EffectsView.effectSlotCount(): Int = effectViews.size

fun EffectsView.displayedEffectCount(): Int = effectViews.count { effectView -> effectView.isEffectIconDisplayed() }
