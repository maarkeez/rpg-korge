package com.mkz.rpg.screen.feedback

/**
 * Ordered playback of [FeedbackBeat]s, driven by [update] (the frame updater), never by coroutines.
 *
 * - With [FeedbackTiming.Instant] every beat is performed inside [enqueue], so behavior is identical to handling the
 *   event directly.
 * - Consecutive beats of the same kind that touch different things (hits on several units, several sparks) play
 *   together as one step, which keeps long CPU turns short. A backlog longer than the turn budget plays faster.
 * - [onPlaybackChanged] is told when playback starts and ends, but only for playback that really takes time.
 * - [onDrained] runs every time the queue empties, so the view can be resynced from the domain.
 */
class FeedbackQueue(
    private val timing: FeedbackTiming = FeedbackTiming.Instant,
    private val performer: (FeedbackBeat) -> Unit,
    private val onPlaybackChanged: (Boolean) -> Unit = {},
    private val onDrained: () -> Unit = {},
) {
    private val pending = ArrayDeque<FeedbackBeat>()
    private var stepRemainingMs = 0.0
    private var stepActive = false
    private var announcedPlaying = false
    private var pumping = false
    private var playbackScale = 1.0

    /** True while beats are waiting or a step is still playing. */
    val isPlaying: Boolean get() = stepActive || pending.isNotEmpty()

    val pendingCount: Int get() = pending.size

    fun enqueue(beat: FeedbackBeat) {
        pending.addLast(beat)
        pump()
    }

    /** Advances playback by [deltaMs] milliseconds. */
    fun update(deltaMs: Double) {
        if (!stepActive) return
        stepRemainingMs -= deltaMs
        if (stepRemainingMs > 0.0) return
        stepActive = false
        pump()
    }

    private fun pump() {
        if (pumping || stepActive) return
        pumping = true
        try {
            while (!stepActive && pending.isNotEmpty()) {
                val step = takeStep()
                step.forEach(performer)
                val stepMs = step.maxOf { timing.durationOf(it) } * speedFactor()
                if (stepMs > 0.0) {
                    stepActive = true
                    stepRemainingMs = stepMs
                }
            }
        } finally {
            pumping = false
        }
        if (isPlaying && !announcedPlaying) {
            announcedPlaying = true
            onPlaybackChanged(true)
        }
        if (!isPlaying) {
            playbackScale = 1.0
            if (announcedPlaying) {
                announcedPlaying = false
                onPlaybackChanged(false)
            }
            onDrained()
        }
    }

    private fun takeStep(): List<FeedbackBeat> {
        val first = pending.removeFirst()
        val step = mutableListOf(first)
        while (pending.isNotEmpty() && canPlayTogether(first, pending.first())) step += pending.removeFirst()
        return step
    }

    private fun canPlayTogether(
        first: FeedbackBeat,
        next: FeedbackBeat,
    ): Boolean =
        when (first) {
            is FeedbackBeat.Hit -> next is FeedbackBeat.Hit && next.battleUnitId != first.battleUnitId
            is FeedbackBeat.Heal -> next is FeedbackBeat.Heal && next.battleUnitId != first.battleUnitId
            is FeedbackBeat.StatusApplied -> next is FeedbackBeat.StatusApplied && next.battleUnitId != first.battleUnitId
            is FeedbackBeat.Spread -> next is FeedbackBeat.Spread
            is FeedbackBeat.Deployed -> next is FeedbackBeat.Deployed
            else -> false
        }

    /** Only ever speeds up while a backlog plays, so the whole backlog fits the budget. Resets once the queue empties. */
    private fun speedFactor(): Double {
        if (timing.turnBudgetMs <= 0) return 1.0
        val backlogMs = pending.sumOf { timing.durationOf(it) }
        if (backlogMs > timing.turnBudgetMs) playbackScale = minOf(playbackScale, timing.turnBudgetMs.toDouble() / backlogMs)
        return playbackScale
    }
}
