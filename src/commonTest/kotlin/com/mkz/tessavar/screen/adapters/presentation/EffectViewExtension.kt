package com.mkz.tessavar.screen.adapters.presentation

fun EffectView.effectIconCount(): Int = children.count { child -> child.name == EffectView.EFFECT }

fun EffectView.isEffectIconDisplayed(): Boolean = visible && effectIconCount() > 0

fun EffectView.isBlinking(): Boolean = blinkAnimator?.isActive == true

fun EffectView.iconAlpha(): Double = alpha
