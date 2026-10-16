package com.mkz.rpg.screen.adapters.presentation

// Page object of [EffectsView]. Use `EffectViewExtension` to inspect each effect.

fun EffectsView.effectSlotCount(): Int = effectViews.size

fun EffectsView.displayedEffectCount(): Int = effectViews.count { effectView -> effectView.isEffectIconDisplayed() }

/** Whether each displayed effect is blinking, in display order. */
fun EffectsView.blinkingEffects(): List<Boolean> = effectViews.filter { effectView -> effectView.isEffectIconDisplayed() }.map { effectView -> effectView.isBlinking() }

/** Whether the plus sign is displayed, meaning there are more effects than icons. */
fun EffectsView.isOverflowDisplayed(): Boolean = overflowLabel.visible
