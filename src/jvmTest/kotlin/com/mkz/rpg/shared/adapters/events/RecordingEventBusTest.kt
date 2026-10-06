package com.mkz.rpg.shared.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.subscribe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordingEventBusTest {
    private val delegate = InMemoryEventBus()
    private val recordingEventBus = RecordingEventBus(delegate)

    @Nested
    inner class Publish {
        @Test
        fun `should record the event when a single event is published`() {
            // Given
            val event = BattleEvent.RequestFinishPlayerTurn
            // When
            recordingEventBus.publish(event)
            // Then
            assertThat(recordingEventBus.events).containsExactly(event)
        }

        @Test
        fun `should record all events in order when a set of events is published`() {
            // Given
            val eventOne = BattleEvent.RequestFinishPlayerTurn
            val eventTwo =
                BattleUnitEvent.RequestMoveBattleUnit(
                    battleUnitId = "battle-unit-1",
                    moveToRow = 0,
                    moveToColumn = 0,
                )
            // When
            recordingEventBus.publish(setOf(eventOne, eventTwo))
            // Then
            assertThat(recordingEventBus.events).containsExactly(eventOne, eventTwo)
        }
    }

    @Nested
    inner class Dispatch {
        @Test
        fun `should deliver the event to the handler when dispatched`() {
            // Given
            val received = mutableListOf<BattleEvent.RequestFinishPlayerTurn>()
            recordingEventBus.subscribe<BattleEvent.RequestFinishPlayerTurn> { received += it }
            recordingEventBus.publish(BattleEvent.RequestFinishPlayerTurn)
            // When
            recordingEventBus.dispatch()
            // Then
            assertThat(received).containsExactly(BattleEvent.RequestFinishPlayerTurn)
        }
    }

    @Nested
    inner class Events {
        @Test
        fun `should return a snapshot of the recorded events when events is read`() {
            // Given
            recordingEventBus.publish(BattleEvent.RequestFinishPlayerTurn)
            val firstRead = recordingEventBus.events
            // When
            recordingEventBus.publish(
                BattleUnitEvent.RequestMoveBattleUnit(
                    battleUnitId = "battle-unit-1",
                    moveToRow = 1,
                    moveToColumn = 1,
                ),
            )
            // Then
            assertThat(firstRead).hasSize(1)
            assertThat(recordingEventBus.events).hasSize(2)
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `should remove all recorded events when cleared`() {
            // Given
            recordingEventBus.publish(BattleEvent.RequestFinishPlayerTurn)
            // When
            recordingEventBus.clear()
            // Then
            assertThat(recordingEventBus.events).isEmpty()
        }
    }
}
