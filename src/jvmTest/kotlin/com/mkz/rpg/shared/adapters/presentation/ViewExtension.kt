package com.mkz.rpg.shared.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.View

// Shared helpers for the view page objects (`<View>Extension.kt` files).

/** Clicks the [view] with the mouse, at the center of the view on the screen. The [view] must be on the stage. */
suspend fun ViewsForTesting.clickOn(view: View) {
    val center = view.getGlobalBounds().center
    mouseMoveAndClickTo(center.x, center.y)
}

/** Whether the [View] and all its parents are visible. */
fun View.isShown(): Boolean = generateSequence(this) { it.parent }.all { it.visible && it.alpha > 0 }

/** The [View] and all its descendants, in depth-first order. */
fun View.descendants(): List<View> = listOf(this) + (this as? korlibs.korge.view.Container)?.children.orEmpty().flatMap { it.descendants() }
