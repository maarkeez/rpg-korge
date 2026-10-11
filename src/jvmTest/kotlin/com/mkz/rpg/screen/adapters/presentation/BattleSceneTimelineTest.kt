package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleSceneTimelineTest {
    private val timeline = battleSceneTimeline()

    @Nested
    inner class Play {
        @Test
        fun `should play the step straight away when the timeline is idle`() {
            // Given
            val playedSteps = mutableListOf<String>()
            // When
            timeline.play { playedSteps += "step" }
            // Then
            assertThat(playedSteps).containsExactly("step")
        }

        @Test
        fun `should wait to play the step when the previous step is still playing`() {
            // Given
            val playedSteps = mutableListOf<String>()
            timeline.playLongStep()
            // When
            timeline.play { playedSteps += "step" }
            // Then
            assertThat(playedSteps).isEmpty()
        }

        @Test
        fun `should play the waiting steps in order when the previous step finishes`() {
            // Given
            val playedSteps = mutableListOf<String>()
            val longStep = timeline.playLongStep()
            timeline.play { playedSteps += "first" }
            timeline.play { playedSteps += "second" }
            // When
            longStep.complete(Unit)
            // Then
            assertThat(playedSteps).containsExactly("first", "second")
        }

        @Test
        fun `should play the step after the previous steps when it is added by a playing step`() {
            // Given
            val playedSteps = mutableListOf<String>()
            // When
            timeline.play {
                timeline.play { playedSteps += "added" }
                playedSteps += "adding"
            }
            // Then
            assertThat(playedSteps).containsExactly("adding", "added")
        }
    }

    @Nested
    inner class IsPlaying {
        @Test
        fun `should not be playing when no step has been added`() {
            // Given
            // When
            val isPlaying = timeline.isPlaying
            // Then
            assertThat(isPlaying).isFalse
        }

        @Test
        fun `should be playing when a step is still playing`() {
            // Given
            timeline.playLongStep()
            // When
            val isPlaying = timeline.isPlaying
            // Then
            assertThat(isPlaying).isTrue
        }

        @Test
        fun `should not be playing when every step has been played`() {
            // Given
            val longStep = timeline.playLongStep()
            timeline.play { }
            longStep.complete(Unit)
            // When
            val isPlaying = timeline.isPlaying
            // Then
            assertThat(isPlaying).isFalse
        }
    }
}
