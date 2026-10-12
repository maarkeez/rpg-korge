package com.mkz.rpg.shared.adapters.e2e

import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// Shared helpers for the end-to-end tests.

private val WAIT_TIMEOUT = 10.seconds

/**
 * Waits until the condition is met, or the timeout is reached.
 *
 * The game loads its resources asynchronously in real time, while the frames are simulated much faster than that.
 * Hence, waiting is bounded by real time instead of frames. Each check waits for the next frame, so the game runs in between.
 */
suspend fun ViewsForTesting.waitUntil(condition: () -> Boolean) {
    val start = TimeSource.Monotonic.markNow()
    while (!condition() && start.elapsedNow() < WAIT_TIMEOUT) {
        delay(frameTime)
    }
}
