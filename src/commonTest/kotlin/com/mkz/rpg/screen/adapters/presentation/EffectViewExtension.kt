package com.mkz.rpg.screen.adapters.presentation

// Page object of [EffectView].

fun EffectView.effectIconCount(): Int = children.count { child -> child.name == EffectView.EFFECT }

fun EffectView.isEffectIconDisplayed(): Boolean = visible && effectIconCount() > 0

/** Whether the effect is blinking, e.g.: as a new effect an ability cast would apply. */
fun EffectView.isBlinking(): Boolean = blinkAnimator?.isActive == true

fun EffectView.iconAlpha(): Double = alpha
