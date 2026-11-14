package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.QuestInfoMother.completedQuestInfo
import com.mkz.tessavar.screen.adapters.presentation.QuestInfoMother.questInfo
import com.mkz.tessavar.screen.adapters.presentation.QuestInfoMother.task
import com.mkz.tessavar.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import com.mkz.tessavar.shared.adapters.presentation.startAnimation
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestTrackerViewTest : ViewsForTesting() {
    private class RecordingDelegate : QuestTrackerView.Delegate {
        var selections = 0

        override fun questTrackerSelected() {
            selections++
        }
    }

    @Test
    fun `display should draw the task when the quest is active`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            // When
            trackerView.display(questInfo())
            // Then
            trackerView.displayedTask() shouldBe "Defeat the Goat Wizard"
            trackerView.isTaskGlyphShown().shouldBeTrue()
            trackerView.isTickShown().shouldBeFalse()
            trackerView.isChevronShown().shouldBeTrue()
        }

    @Test
    fun `display should draw the pin when a side quest is pinned`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            // When
            trackerView.display(questInfo(isMain = false, isPinned = true))
            val rightEdge = trackerView.capsuleRightEdge()
            val isPinShownOnASideQuest = trackerView.isPinShown()
            trackerView.display(questInfo(isPinned = true))
            // Then
            isPinShownOnASideQuest.shouldBeTrue()
            trackerView.isPinShown().shouldBeFalse()
            trackerView.capsuleRightEdge() shouldBe (rightEdge plusOrMinus 0.01)
        }

    @Test
    fun `display should cut the task with an ellipsis when it is longer than 200`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            // When
            trackerView.display(questInfo(tasks = listOf(task(text = "Derrota al mago cabra de la guarida del sur de las montañas"))))
            // Then
            trackerView.displayedTask()!! shouldEndWith "…"
            trackerView.displayedTaskWidth() shouldBeLessThanOrEqual QuestTrackerView.MAXIMUM_TEXT_WIDTH
        }

    @Test
    fun `display should draw the count only when the task counts above 1`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            // When
            trackerView.display(questInfo(tasks = listOf(task(text = "Defeat 5 Rats", defeatsCounted = 3, requiredDefeats = 5))))
            val counted = trackerView.displayedCount()
            trackerView.display(questInfo())
            // Then
            counted shouldBe "3/5"
            trackerView.displayedCount().shouldBeNull()
        }

    @Test
    fun `display should draw Quest completed and a tick when the quest is completed`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            // When
            trackerView.display(completedQuestInfo())
            // Then
            trackerView.displayedTask() shouldBe "Quest completed"
            trackerView.isTickShown().shouldBeTrue()
            trackerView.isTaskGlyphShown().shouldBeFalse()
            trackerView.displayedCount().shouldBeNull()
        }

    @Test
    fun `setTappable should hide the chevron and keep the right edge when it is not tappable`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.display(questInfo())
            val rightEdge = trackerView.capsuleRightEdge()
            // When
            trackerView.setTappable(false)
            // Then
            trackerView.isChevronShown().shouldBeFalse()
            trackerView.capsuleRightEdge() shouldBe (rightEdge plusOrMinus 0.01)
        }

    @Test
    fun `QuestTrackerView should call its delegate when the capsule is tapped`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.setDelegate(delegate)
            trackerView.display(questInfo())
            addChild(trackerView)
            // When
            clickCapsule(trackerView)
            // Then
            delegate.selections shouldBe 1
        }

    @Test
    fun `QuestTrackerView should not call its delegate when the capsule is tapped and it is not tappable`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.setDelegate(delegate)
            trackerView.display(questInfo())
            trackerView.setTappable(false)
            addChild(trackerView)
            // When
            clickCapsule(trackerView)
            // Then
            delegate.selections shouldBe 0
        }

    @Test
    fun `pop should grow the capsule and shrink it back`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.display(questInfo())
            addChild(trackerView)
            // When
            val popping = startAnimation { trackerView.pop() }
            val scales = sampleUntilCompletion(popping) { trackerView.capsuleScale() }
            // Then
            scales.max() shouldBe (QuestTrackerView.POP_SCALE plusOrMinus 0.05)
            trackerView.capsuleScale() shouldBe 1.0
        }

    @Test
    fun `hide should keep it hidden when a quest is displayed afterwards`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            addChild(trackerView)
            // When
            trackerView.hide()
            trackerView.display(questInfo())
            // Then
            trackerView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `displayEmpty should draw No quest pinned with the chevron and no pin, glyph or count`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.display(questInfo(isMain = false, isPinned = true))
            val rightEdge = trackerView.capsuleRightEdge()
            // When
            trackerView.displayEmpty()
            // Then
            trackerView.displayedTask() shouldBe "No quest pinned"
            trackerView.isChevronShown().shouldBeTrue()
            trackerView.isPinShown().shouldBeFalse()
            trackerView.isTaskGlyphShown().shouldBeFalse()
            trackerView.displayedCount().shouldBeNull()
            trackerView.capsuleRightEdge() shouldBe (rightEdge plusOrMinus 0.01)
        }

    @Test
    fun `setTappable should hide the chevron of the empty tracker when it is not tappable`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.displayEmpty()
            // When
            trackerView.setTappable(false)
            // Then
            trackerView.displayedTask() shouldBe "No quest pinned"
            trackerView.isChevronShown().shouldBeFalse()
        }

    @Test
    fun `clear should draw nothing until a quest is displayed again`() =
        sharedViewsTest {
            // Given
            val trackerView = QuestTrackerView(MessagesMother.messages())
            trackerView.display(questInfo())
            // When
            trackerView.clear()
            val taskWhenCleared = trackerView.displayedTask()
            trackerView.setTappable(false)
            val taskWhenClearedAndReadOnly = trackerView.displayedTask()
            trackerView.display(questInfo())
            // Then
            taskWhenCleared.shouldBeNull()
            taskWhenClearedAndReadOnly.shouldBeNull()
            trackerView.displayedTask() shouldBe "Defeat the Goat Wizard"
        }
}
