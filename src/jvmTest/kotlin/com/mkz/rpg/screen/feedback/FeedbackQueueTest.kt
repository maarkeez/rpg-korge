package com.mkz.rpg.screen.feedback

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeedbackQueueTest {
    private val performed = mutableListOf<FeedbackBeat>()
    private val playbackChanges = mutableListOf<Boolean>()
    private var drainedCount = 0

    private fun queue(timing: FeedbackTiming) =
        FeedbackQueue(
            timing = timing,
            performer = { beat -> performed += beat },
            onPlaybackChanged = { playing -> playbackChanges += playing },
            onDrained = { drainedCount++ },
        )

    private fun hit(
        battleUnitId: String,
        amount: Int = 5,
    ) = FeedbackBeat.Hit(battleUnitId = battleUnitId, amount = amount, remainingHealthPoints = 10)

    private fun move(battleUnitId: String) = FeedbackBeat.Move(battleUnitId, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1)

    @Nested
    inner class Enqueue {
        @Test
        fun `should perform every beat in order immediately when the timing is instant`() {
            // Given
            val queue = queue(FeedbackTiming.Instant)
            val beats = listOf(move("knight"), hit("rat"), FeedbackBeat.Defeated("rat", row = 1, column = 1), hit("knight"))
            // When
            beats.forEach(queue::enqueue)
            // Then
            assertThat(performed).containsExactlyElementsOf(beats)
            assertThat(queue.isPlaying).isFalse()
            assertThat(playbackChanges).isEmpty()
            assertThat(drainedCount).isZero()
        }

        @Test
        fun `should hold later beats back when an earlier beat is still playing`() {
            // Given
            val queue = queue(FeedbackTiming.Standard)
            // When
            queue.enqueue(move("knight"))
            queue.enqueue(hit("rat"))
            // Then
            assertThat(performed).containsExactly(move("knight"))
            assertThat(queue.isPlaying).isTrue()
            assertThat(queue.pendingCount).isEqualTo(1)
            assertThat(playbackChanges).containsExactly(true)
        }

        @Test
        fun `should play hits on different battle units together when they are consecutive`() {
            // Given
            val queue = queue(FeedbackTiming.Standard)
            queue.enqueue(move("bee"))
            queue.enqueue(hit("rat-a"))
            queue.enqueue(hit("rat-b"))
            queue.enqueue(hit("rat-a"))
            // When
            queue.update(deltaMs = 1_000.0)
            // Then
            assertThat(performed).containsExactly(move("bee"), hit("rat-a"), hit("rat-b"))
            assertThat(queue.pendingCount).isEqualTo(1)
        }

        @Test
        fun `should not play two hits on the same battle unit together when they are consecutive`() {
            // Given
            val queue = queue(FeedbackTiming.Standard)
            // When
            queue.enqueue(hit("rat", amount = 3))
            queue.enqueue(hit("rat", amount = 4))
            // Then
            assertThat(performed).containsExactly(hit("rat", amount = 3))
            assertThat(queue.pendingCount).isEqualTo(1)
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should drain the queue and announce the end of playback when enough time has passed`() {
            // Given
            val queue = queue(FeedbackTiming.Standard)
            queue.enqueue(move("knight"))
            queue.enqueue(hit("rat"))
            // When
            queue.update(deltaMs = 10_000.0)
            queue.update(deltaMs = 10_000.0)
            // Then
            assertThat(performed).containsExactly(move("knight"), hit("rat"))
            assertThat(queue.isPlaying).isFalse()
            assertThat(playbackChanges).containsExactly(true, false)
            assertThat(drainedCount).isEqualTo(1)
        }

        @Test
        fun `should keep waiting when less than the beat duration has passed`() {
            // Given
            val queue = queue(FeedbackTiming.Standard)
            queue.enqueue(hit("rat"))
            queue.enqueue(FeedbackBeat.Defeated("rat", row = 0, column = 0))
            // When
            queue.update(deltaMs = 1.0)
            // Then
            assertThat(performed).containsExactly(hit("rat"))
            assertThat(queue.isPlaying).isTrue()
        }

        @Test
        fun `should play a long backlog faster when it exceeds the turn budget`() {
            // Given
            val timing = FeedbackTiming.Standard.copy(turnBudgetMs = 500)
            val queue = queue(timing)
            repeat(10) { index -> queue.enqueue(FeedbackBeat.Defeated("unit-$index", row = index, column = 0)) }
            // When
            var elapsedMs = 0
            while (queue.isPlaying && elapsedMs < 10_000) {
                queue.update(deltaMs = 10.0)
                elapsedMs += 10
            }
            // Then
            assertThat(performed).hasSize(10)
            assertThat(queue.isPlaying).isFalse()
            assertThat(elapsedMs).isLessThan(1_000)
        }
    }
}
