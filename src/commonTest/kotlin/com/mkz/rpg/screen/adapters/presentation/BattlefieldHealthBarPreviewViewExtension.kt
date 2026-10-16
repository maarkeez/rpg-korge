package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.animate.Animator

// Page object of [BattlefieldHealthBarPreviewView].

/** The blinks of the health preview segments, to check later whether they are still blinking. */
fun BattlefieldHealthBarPreviewView.blinks(): List<Animator> = blinkAnimators.toList()

fun Animator.isBlinking(): Boolean = isActive

/** How many health preview segments are blinking. */
fun BattlefieldHealthBarPreviewView.blinkingCount(): Int = blinkAnimators.count { animator -> animator.isBlinking() }
