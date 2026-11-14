package com.mkz.tessavar.screen.adapters.presentation

import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class GameSceneTimeline(
    private val coroutineScope: CoroutineScope,
) {
    private val steps = ArrayDeque<suspend () -> Unit>()
    private var isPlayingSteps = false

    private val togetherSteps = ArrayDeque<Job>()
    private val playingListeners = mutableListOf<(isPlaying: Boolean) -> Unit>()

    val isPlaying: Boolean
        get() = isPlayingSteps || steps.isNotEmpty()

    fun onPlayingChanged(listener: (isPlaying: Boolean) -> Unit) {
        playingListeners += listener
    }

    fun play(step: suspend () -> Unit) {
        steps.addLast(step)
        if (isPlayingSteps) return
        isPlayingSteps = true
        playingListeners.forEach { listener -> listener(true) }
        coroutineScope.launchImmediately {
            try {
                while (steps.isNotEmpty()) {
                    steps.removeFirst().invoke()
                    while (togetherSteps.isNotEmpty()) togetherSteps.removeFirst().join()
                }
            } finally {
                isPlayingSteps = false
                playingListeners.forEach { listener -> listener(false) }
            }
        }
    }

    fun playNext(step: suspend () -> Unit) {
        if (!isPlayingSteps) return play(step)
        steps.addFirst(step)
    }

    fun playTogether(step: suspend () -> Unit) {
        if (!isPlayingSteps) return play(step)
        togetherSteps.addLast(coroutineScope.launchImmediately { step() })
    }

    fun playAlongside(step: suspend () -> Unit) {
        coroutineScope.launchImmediately { step() }
    }
}
