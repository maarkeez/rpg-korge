package com.mkz.rpg.screen.adapters.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object GameSceneTimelineMother {
    // Plays the steps in the calling thread, so steps that do not suspend are played straight away.
    fun gameSceneTimeline() = GameSceneTimeline(CoroutineScope(Dispatchers.Unconfined))

    /** Keeps the timeline playing until the returned step is completed, e.g.: a unit still walking. */
    fun GameSceneTimeline.playLongStep(): CompletableDeferred<Unit> {
        val longStep = CompletableDeferred<Unit>()
        play { longStep.await() }
        return longStep
    }
}
