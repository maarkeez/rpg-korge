package com.mkz.rpg.screen.adapters.presentation

import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CoroutineScope

/**
 * Plays the visual steps of the battle scene one after another, in the order they are added.
 *
 * The game resolves its events instantly (e.g.: a whole CPU turn), while some steps take time (e.g.: a unit walking
 * tile by tile). Hence, presenters read what they need when an event arrives and add a step that draws it once the
 * previous steps have been played.
 */
class BattleSceneTimeline(
    private val coroutineScope: CoroutineScope,
) {
    private val steps = ArrayDeque<suspend () -> Unit>()
    private var isPlayingSteps = false

    /** Whether a step is being played or waiting to be played. The player should not act on the scene meanwhile. */
    val isPlaying: Boolean
        get() = isPlayingSteps || steps.isNotEmpty()

    /** Adds a step to be played after the previous ones. When the timeline is idle, the step starts straight away. */
    fun play(step: suspend () -> Unit) {
        steps.addLast(step)
        if (isPlayingSteps) return
        isPlayingSteps = true
        coroutineScope.launchImmediately {
            try {
                while (steps.isNotEmpty()) {
                    steps.removeFirst().invoke()
                }
            } finally {
                isPlayingSteps = false
            }
        }
    }
}
