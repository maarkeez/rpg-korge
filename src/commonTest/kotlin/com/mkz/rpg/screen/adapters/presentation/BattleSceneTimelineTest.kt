package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test

class BattleSceneTimelineTest {
    private val timeline = battleSceneTimeline()

    @Test
    fun `Play should play the step straight away when the timeline is idle`() {
        // Given
        val playedSteps = mutableListOf<String>()
        // When
        timeline.play { playedSteps += "step" }
        // Then
        playedSteps.shouldContainExactly("step")
    }

    @Test
    fun `Play should wait to play the step when the previous step is still playing`() {
        // Given
        val playedSteps = mutableListOf<String>()
        timeline.playLongStep()
        // When
        timeline.play { playedSteps += "step" }
        // Then
        playedSteps.shouldBeEmpty()
    }

    @Test
    fun `Play should play the waiting steps in order when the previous step finishes`() {
        // Given
        val playedSteps = mutableListOf<String>()
        val longStep = timeline.playLongStep()
        timeline.play { playedSteps += "first" }
        timeline.play { playedSteps += "second" }
        // When
        longStep.complete(Unit)
        // Then
        playedSteps.shouldContainExactly("first", "second")
    }

    @Test
    fun `Play should play the step after the previous steps when it is added by a playing step`() {
        // Given
        val playedSteps = mutableListOf<String>()
        // When
        timeline.play {
            timeline.play { playedSteps += "added" }
            playedSteps += "adding"
        }
        // Then
        playedSteps.shouldContainExactly("adding", "added")
    }

    @Test
    fun `OnPlayingChanged should notify when the timeline starts and stops playing`() {
        // Given
        val notifications = mutableListOf<Boolean>()
        timeline.onPlayingChanged { isPlaying -> notifications += isPlaying }
        val longStep = timeline.playLongStep()
        timeline.play { }
        // When
        longStep.complete(Unit)
        // Then
        notifications.shouldContainExactly(true, false)
    }

    @Test
    fun `OnPlayingChanged should notify that the timeline is playing when a step is still playing`() {
        // Given
        val notifications = mutableListOf<Boolean>()
        timeline.onPlayingChanged { isPlaying -> notifications += isPlaying }
        // When
        timeline.playLongStep()
        // Then
        notifications.shouldContainExactly(true)
    }

    @Test
    fun `IsPlaying should not be playing when no step has been added`() {
        // Given
        // When
        val isPlaying = timeline.isPlaying
        // Then
        isPlaying.shouldBeFalse()
    }

    @Test
    fun `IsPlaying should be playing when a step is still playing`() {
        // Given
        timeline.playLongStep()
        // When
        val isPlaying = timeline.isPlaying
        // Then
        isPlaying.shouldBeTrue()
    }

    @Test
    fun `IsPlaying should not be playing when every step has been played`() {
        // Given
        val longStep = timeline.playLongStep()
        timeline.play { }
        longStep.complete(Unit)
        // When
        val isPlaying = timeline.isPlaying
        // Then
        isPlaying.shouldBeFalse()
    }

    @Test
    fun `PlayAlongside should play the step straight away when another step is still playing`() {
        // Given
        val playedSteps = mutableListOf<String>()
        timeline.playLongStep()
        // When
        timeline.playAlongside { playedSteps += "alongside" }
        // Then
        playedSteps.shouldContainExactly("alongside")
    }

    @Test
    fun `PlayAlongside should not make the timeline playing while its step plays`() {
        // Given
        val alongsideStep = CompletableDeferred<Unit>()
        // When
        timeline.playAlongside { alongsideStep.await() }
        // Then
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `PlayTogether should play the step straight away when another step is still playing`() {
        // Given
        val playedSteps = mutableListOf<String>()
        timeline.playLongStep()
        // When
        timeline.playTogether { playedSteps += "together" }
        // Then
        playedSteps.shouldContainExactly("together")
    }

    @Test
    fun `PlayTogether should play the next steps once both steps are done`() {
        // Given
        val playedSteps = mutableListOf<String>()
        val longStep = timeline.playLongStep()
        val togetherStep = CompletableDeferred<Unit>()
        timeline.playTogether { togetherStep.await() }
        timeline.play { playedSteps += "next" }
        // When
        longStep.complete(Unit)
        // Then
        playedSteps.shouldBeEmpty()
        timeline.isPlaying.shouldBeTrue()
        togetherStep.complete(Unit)
        playedSteps.shouldContainExactly("next")
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `PlayTogether should play the step as a step of the timeline when the timeline is idle`() {
        // Given
        val togetherStep = CompletableDeferred<Unit>()
        // When
        timeline.playTogether { togetherStep.await() }
        // Then
        timeline.isPlaying.shouldBeTrue()
    }
}
