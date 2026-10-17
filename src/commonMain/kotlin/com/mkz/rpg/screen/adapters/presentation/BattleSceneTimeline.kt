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
    private val playingListeners = mutableListOf<(isPlaying: Boolean) -> Unit>()

    /** Whether a step is being played or waiting to be played. The player should not act on the scene meanwhile. */
    val isPlaying: Boolean
        get() = isPlayingSteps || steps.isNotEmpty()

    /** Calls the listener every time the timeline starts or stops playing, e.g.: to disable what the player can not use. */
    fun onPlayingChanged(listener: (isPlaying: Boolean) -> Unit) {
        playingListeners += listener
    }

    /** Adds a step to be played after the previous ones. When the timeline is idle, the step starts straight away. */
    fun play(step: suspend () -> Unit) {
        steps.addLast(step)
        if (isPlayingSteps) return
        isPlayingSteps = true
        playingListeners.forEach { listener -> listener(true) }
        coroutineScope.launchImmediately {
            try {
                while (steps.isNotEmpty()) {
                    steps.removeFirst().invoke()
                }
            } finally {
                isPlayingSteps = false
                playingListeners.forEach { listener -> listener(false) }
            }
        }
    }

    /**
     * Plays the step straight away, alongside the other steps, e.g.: a CPU battle unit wandering while the party walks.
     * The timeline is not playing because of it, so the player can still act meanwhile.
     */
    fun playAlongside(step: suspend () -> Unit) {
        coroutineScope.launchImmediately { step() }
    }
}
