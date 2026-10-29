package com.mkz.rpg.shared.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Container
import korlibs.korge.view.View
import korlibs.korge.view.container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

// Shared helpers for the view page objects (`<View>Extension.kt` files).

/** Clicks the [view] with the mouse, at the center of the view on the screen. The [view] must be on the stage. */
suspend fun ViewsForTesting.clickOn(view: View) {
    val center = view.getGlobalBounds().center
    mouseMoveAndClickTo(center.x, center.y)
}

/** Adds the [view] in a container drawn [scale] times its size, e.g.: as the Storybook zoomed in draws it. */
fun Container.addChildScaled(
    view: View,
    scale: Double,
) {
    container().also { scaled -> scaled.scale = scale }.addChild(view)
}

/** Whether the [View] and all its parents are visible. */
fun View.isShown(): Boolean = generateSequence(this) { it.parent }.all { it.visible && it.alpha > 0 }

/** The [View] and all its descendants, in depth-first order. */
fun View.descendants(): List<View> = listOf(this) + (this as? korlibs.korge.view.Container)?.children.orEmpty().flatMap { it.descendants() }

private const val MAX_ANIMATION_FRAMES = 1_000

/**
 * Starts a view animation (e.g.: a battle unit moving) in another coroutine, so the test can check what is displayed
 * while it plays. The animated view must be on the stage.
 */
suspend fun startAnimation(animation: suspend () -> Unit): Job = CoroutineScope(currentCoroutineContext()).launch(start = CoroutineStart.UNDISPATCHED) { animation() }

/** Advances one frame. Animations run in another coroutine, which only runs when the test yields. */
suspend fun ViewsForTesting.nextFrame() {
    delayFrame()
    yield()
}

/** Advances frames until the animation finishes. */
suspend fun ViewsForTesting.awaitCompletion(animation: Job) {
    var frames = 0
    while (!animation.isCompleted && frames++ < MAX_ANIMATION_FRAMES) nextFrame()
}

/** Advances frames until the animation finishes, sampling [sample] before every frame. */
suspend fun <T> ViewsForTesting.sampleUntilCompletion(
    animation: Job,
    sample: () -> T,
): List<T> {
    val samples = mutableListOf<T>()
    while (!animation.isCompleted && samples.size < MAX_ANIMATION_FRAMES) {
        samples += sample()
        nextFrame()
    }
    return samples
}
