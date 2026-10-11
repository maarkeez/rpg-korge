package com.mkz.rpg.screen.adapters.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object BattleSceneTimelineMother {
    // Plays the steps in the calling thread, so steps that do not suspend are played straight away.
    fun battleSceneTimeline() = BattleSceneTimeline(CoroutineScope(Dispatchers.Unconfined))

    /** Keeps the timeline playing until the returned step is completed, e.g.: a unit still walking. */
    fun BattleSceneTimeline.playLongStep(): CompletableDeferred<Unit> {
        val longStep = CompletableDeferred<Unit>()
        play { longStep.await() }
        return longStep
    }
}
