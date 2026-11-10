package com.mkz.rpg.screen.adapters.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object GameSceneTimelineMother {
    fun gameSceneTimeline() = GameSceneTimeline(CoroutineScope(Dispatchers.Unconfined))

    fun GameSceneTimeline.playLongStep(): CompletableDeferred<Unit> {
        val longStep = CompletableDeferred<Unit>()
        play { longStep.await() }
        return longStep
    }
}
