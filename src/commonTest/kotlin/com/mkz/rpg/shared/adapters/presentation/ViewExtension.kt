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

suspend fun ViewsForTesting.clickOn(view: View) {
    val center = view.getGlobalBounds().center
    mouseMoveAndClickTo(center.x, center.y)
}

fun Container.addChildScaled(
    view: View,
    scale: Double,
) {
    container().also { scaled -> scaled.scale = scale }.addChild(view)
}

fun View.isShown(): Boolean = generateSequence(this) { it.parent }.all { it.visible && it.alpha > 0 }

fun View.descendants(): List<View> = listOf(this) + (this as? korlibs.korge.view.Container)?.children.orEmpty().flatMap { it.descendants() }

private const val MAX_ANIMATION_FRAMES = 1_000

suspend fun startAnimation(animation: suspend () -> Unit): Job = CoroutineScope(currentCoroutineContext()).launch(start = CoroutineStart.UNDISPATCHED) { animation() }

suspend fun ViewsForTesting.nextFrame() {
    delayFrame()
    yield()
}

suspend fun ViewsForTesting.awaitCompletion(animation: Job) {
    var frames = 0
    while (!animation.isCompleted && frames++ < MAX_ANIMATION_FRAMES) nextFrame()
}

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
