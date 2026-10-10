package com.mkz.rpg.screen.feedback

import korlibs.korge.view.Container
import korlibs.korge.view.View

/**
 * Short-lived pixel effects drawn over the battlefield (flashes, numbers, puffs, sparks).
 * Effects are advanced by [advance], called from the frame updater, and remove themselves when they end.
 * Nothing here reads or changes battle state.
 */
class FxLayer : Container() {
    private class Effect(
        val view: View,
        val durationMs: Double,
        val onProgress: (Double) -> Unit,
    ) {
        var elapsedMs = 0.0
    }

    private val active = mutableListOf<Effect>()

    val activeCount: Int get() = active.size

    init {
        mouseEnabled = false
        mouseChildren = false
    }

    /** Shows [view] for [durationMs]. [onProgress] gets 0.0 when it starts and 1.0 on the last frame. */
    fun play(
        view: View,
        durationMs: Int,
        onProgress: (Double) -> Unit = {},
    ) {
        addChild(view)
        active += Effect(view, durationMs.toDouble().coerceAtLeast(1.0), onProgress)
        onProgress(0.0)
    }

    fun advance(deltaMs: Double) {
        if (active.isEmpty()) return
        val finished = mutableListOf<Effect>()
        active.toList().forEach { effect ->
            effect.elapsedMs += deltaMs
            if (effect.elapsedMs >= effect.durationMs) {
                effect.onProgress(1.0)
                finished += effect
            } else {
                effect.onProgress(effect.elapsedMs / effect.durationMs)
            }
        }
        finished.forEach { effect ->
            effect.view.removeFromParent()
            active -= effect
        }
    }

    fun clearEffects() {
        active.forEach { it.view.removeFromParent() }
        active.clear()
    }
}
