package com.mkz.rpg.screen.feedback

import kotlin.math.max
import kotlin.math.min

/** How long each beat takes, in milliseconds. [Instant] is all zeros and is what tests use. */
data class FeedbackTiming(
    val hopMs: Int,
    val maxMoveMs: Int,
    val teleportMs: Int,
    val hitMs: Int,
    val healMs: Int,
    val statusMs: Int,
    val defeatMs: Int,
    val spreadMs: Int,
    val deployMs: Int,
    val cameraMs: Int,
    /** A backlog longer than this is played faster, so a CPU turn stays short. */
    val turnBudgetMs: Int,
    /** How long each idle frame of a standing unit is shown. Zero keeps every unit on frame 0. */
    val idleFrameMs: Int = 0,
) {
    val isInstant: Boolean get() = this == Instant

    fun durationOf(beat: FeedbackBeat): Int =
        when (beat) {
            is FeedbackBeat.Move -> min(hopMs, if (beat.hops > 0) maxMoveMs / beat.hops else hopMs)
            is FeedbackBeat.Teleport -> teleportMs
            is FeedbackBeat.Hit -> hitMs
            is FeedbackBeat.Heal -> healMs
            is FeedbackBeat.StatusApplied -> statusMs
            is FeedbackBeat.Defeated -> defeatMs
            is FeedbackBeat.Spread -> spreadMs
            is FeedbackBeat.Deployed -> deployMs
            is FeedbackBeat.CameraFocus -> cameraMs
            is FeedbackBeat.TurnStarted,
            is FeedbackBeat.OverlayRefresh,
            is FeedbackBeat.OccupantRemoved,
            -> 0
        }.let { max(0, it) }

    companion object {
        val Instant =
            FeedbackTiming(
                hopMs = 0,
                maxMoveMs = 0,
                teleportMs = 0,
                hitMs = 0,
                healMs = 0,
                statusMs = 0,
                defeatMs = 0,
                spreadMs = 0,
                deployMs = 0,
                cameraMs = 0,
                turnBudgetMs = 0,
            )

        /** Every beat is at most 300 ms, and a whole CPU turn is squeezed to about 3 s. */
        val Standard =
            FeedbackTiming(
                hopMs = 70,
                maxMoveMs = 280,
                teleportMs = 160,
                hitMs = 240,
                healMs = 240,
                statusMs = 160,
                defeatMs = 260,
                spreadMs = 240,
                deployMs = 140,
                cameraMs = 120,
                turnBudgetMs = 3_000,
                idleFrameMs = 500,
            )
    }
}
