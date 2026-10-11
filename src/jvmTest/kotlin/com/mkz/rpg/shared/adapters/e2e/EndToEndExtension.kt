package com.mkz.rpg.shared.adapters.e2e

import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.yield

// Shared helpers for the end-to-end tests.

private const val WAIT_TIMEOUT_MILLIS = 10_000

/**
 * Waits until the condition is met, or the timeout is reached.
 *
 * Frames are simulated much faster than real time, while the game loads its resources asynchronously in real time.
 * Hence, waiting is bounded by real time instead of frames, and `yield()` lets the game coroutines run between frames.
 */
suspend fun ViewsForTesting.waitUntil(condition: () -> Boolean) {
    val startMillis = System.currentTimeMillis()
    while (!condition() && System.currentTimeMillis() - startMillis < WAIT_TIMEOUT_MILLIS) {
        delayFrame()
        yield()
    }
}
