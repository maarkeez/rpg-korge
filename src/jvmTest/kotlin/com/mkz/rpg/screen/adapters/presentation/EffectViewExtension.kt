package com.mkz.rpg.screen.adapters.presentation

// Page object of [EffectView].

fun EffectView.effectIconCount(): Int = children.count { child -> child.name == EffectView.EFFECT }

fun EffectView.isEffectIconDisplayed(): Boolean = visible && effectIconCount() > 0
