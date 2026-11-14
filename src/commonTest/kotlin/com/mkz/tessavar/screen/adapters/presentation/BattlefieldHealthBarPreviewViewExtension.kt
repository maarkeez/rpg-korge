package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.animate.Animator

fun BattlefieldHealthBarPreviewView.blinks(): List<Animator> = blinkAnimators.toList()

fun Animator.isBlinking(): Boolean = isActive

fun BattlefieldHealthBarPreviewView.blinkingCount(): Int = blinkAnimators.count { animator -> animator.isBlinking() }

fun BattlefieldHealthBarPreviewView.blinkedThirdAlphas(): List<Double> = blinkingThirds.map { third -> third.alpha }
