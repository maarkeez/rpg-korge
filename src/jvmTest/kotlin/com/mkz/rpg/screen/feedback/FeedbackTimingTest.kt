package com.mkz.rpg.screen.feedback

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FeedbackTimingTest {
    private val allBeats =
        listOf(
            FeedbackBeat.Move("knight", 0, 0, 0, 1, hops = 1),
            FeedbackBeat.Teleport("knight", 0, 0, 5, 5),
            FeedbackBeat.Hit("rat", amount = 3, remainingHealthPoints = 7),
            FeedbackBeat.Heal("rat", amount = 3, remainingHealthPoints = 10),
            FeedbackBeat.StatusApplied("rat", effectId = "venom-damage"),
            FeedbackBeat.Defeated("rat", row = 1, column = 1),
            FeedbackBeat.Spread(fromRow = 1, fromColumn = 1, toRow = 1, toColumn = 2),
            FeedbackBeat.Deployed("bee", row = 2, column = 2),
            FeedbackBeat.CameraFocus(row = 3, column = 3),
            FeedbackBeat.TurnStarted("player-one"),
            FeedbackBeat.OverlayRefresh("rat"),
            FeedbackBeat.OccupantRemoved(row = 1, column = 1),
        )

    @Test
    fun `should last zero milliseconds for every beat when the timing is instant`() {
        // Given
        val timing = FeedbackTiming.Instant
        // When
        val durations = allBeats.map(timing::durationOf)
        // Then
        assertThat(durations).containsOnly(0)
        assertThat(timing.isInstant).isTrue()
    }

    @Test
    fun `should keep every beat within 300 milliseconds when the timing is standard`() {
        // Given
        val timing = FeedbackTiming.Standard
        // When
        val durations = allBeats.map(timing::durationOf)
        // Then
        assertThat(durations).allMatch { it <= 300 }
        assertThat(timing.isInstant).isFalse()
    }

    @Test
    fun `should shorten each hop so a whole walk fits the movement budget when the walk is long`() {
        // Given
        val timing = FeedbackTiming.Standard
        val hops = 8
        // When
        val hopDuration = timing.durationOf(FeedbackBeat.Move("knight", 0, 0, 0, 1, hops = hops))
        // Then
        assertThat(hopDuration * hops).isLessThanOrEqualTo(timing.maxMoveMs)
    }
}
