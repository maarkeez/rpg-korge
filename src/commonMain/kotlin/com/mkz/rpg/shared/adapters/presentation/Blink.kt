package com.mkz.rpg.shared.adapters.presentation

import korlibs.korge.animate.Animator
import korlibs.korge.animate.alpha
import korlibs.korge.animate.animator
import korlibs.korge.animate.wait
import korlibs.korge.view.View
import korlibs.time.milliseconds

private val BLINK_PAUSE = 200.milliseconds

/**
 * Fades the view to [firstAlpha] and then to [secondAlpha], in a loop, e.g.: to highlight what an action would change.
 * Keeps blinking until the returned animator is cancelled.
 */
fun View.blink(
    firstAlpha: Double,
    secondAlpha: Double,
): Animator =
    animator(parallel = true, looped = true) {
        sequence(looped = true) {
            alpha(this@blink, firstAlpha)
            wait(BLINK_PAUSE)
            alpha(this@blink, secondAlpha)
            wait(BLINK_PAUSE)
        }
    }
