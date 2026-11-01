package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.completedQuestInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.questInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.task
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestDetailViewTest : ViewsForTesting() {
    private val longDescription =
        "Hace mucho tiempo una cabra se comió el libro de hechizos de un mago y se tragó toda su magia. " +
            "Ahora gobierna la guarida del sur, y nadie se atreve a acercarse, ni de día ni de noche, ni siquiera los más valientes del reino."

    private fun detailView() = QuestDetailView(BattleSceneResourcesLoader(), TextsMother.texts())

    @Test
    fun `display should draw the name, the description, the task and the reward when given a quest`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            // When
            detailView.display(questInfo())
            // Then
            detailView.isDetailShown().shouldBeTrue()
            detailView.displayedCaption() shouldBe "MAIN QUEST"
            detailView.displayedName().shouldContainExactly("The Goat Wizard's Lair")
            detailView.displayedDescription().joinToString(" ") shouldBe "A goat swallowed a wizard's magic and now rules the lair down south."
            detailView.displayedTask() shouldBe "Defeat the Goat Wizard"
            detailView.displayedReward().shouldContainExactly("150 G")
        }

    @Test
    fun `display should draw the count and the bar only when the task counts above 1`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            // When
            detailView.display(questInfo(tasks = listOf(task(text = "Defeat 5 Rats", progress = 3, count = 5))))
            val count = detailView.displayedCount()
            val isBarShown = detailView.isProgressBarShown()
            detailView.display(questInfo())
            // Then
            count shouldBe "3/5"
            isBarShown.shouldBeTrue()
            detailView.displayedCount().shouldBeNull()
            detailView.isProgressBarShown().shouldBeFalse()
        }

    @Test
    fun `display should cut the description to the lines beside the target when it is long`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            // When
            detailView.display(questInfo(description = longDescription))
            val linesBesideAShortName = detailView.displayedDescription()
            detailView.display(questInfo(name = "La guarida del mago cabra en las montañas del sur", description = longDescription))
            val linesBesideALongName = detailView.displayedDescription()
            // Then
            linesBesideAShortName shouldHaveSize 4
            linesBesideAShortName.last() shouldEndWith "…"
            detailView.displayedName() shouldHaveSize 2
            linesBesideALongName shouldHaveSize 3
            linesBesideALongName.last() shouldEndWith "…"
        }

    @Test
    fun `display should draw the tasks done only while the quest is active`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            val tasks = listOf(task(text = "Talk to the Elder", progress = 1, isDone = true), task(text = "Reach the lair", progress = 1, isDone = true), task())
            // When
            detailView.display(questInfo(tasks = tasks))
            val tasksDone = detailView.displayedTasksDone()
            detailView.display(questInfo())
            val noTasksDone = detailView.displayedTasksDone()
            detailView.display(completedQuestInfo())
            // Then
            tasksDone shouldBe "2 tasks done"
            noTasksDone.shouldBeNull()
            detailView.displayedTasksDone().shouldBeNull()
        }

    @Test
    fun `display should draw QUEST COMPLETED and a ticked target when the quest is completed`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            // When
            detailView.display(completedQuestInfo())
            // Then
            detailView.displayedCaption() shouldBe "QUEST COMPLETED"
            detailView.isTargetTicked().shouldBeTrue()
            detailView.displayedTask() shouldBe "Defeat the Goat Wizard"
            detailView.displayedCount().shouldBeNull()
        }

    @Test
    fun `display should name a side quest in the caption when it is not the main quest`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            // When
            detailView.display(questInfo(isMain = false))
            // Then
            detailView.displayedCaption() shouldBe "SIDE QUEST"
        }

    @Test
    fun `display should show the pill when it is repeatable`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            // When
            detailView.display(questInfo(isMain = false, isRepeatable = true))
            val isPillShown = detailView.isRepeatablePillShown()
            detailView.display(questInfo(isMain = false))
            // Then
            isPillShown.shouldBeTrue()
            detailView.isRepeatablePillShown().shouldBeFalse()
        }

    @Test
    fun `display should draw the reward above the buttons when it is a large reward`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            val items = listOf("rune-hammer", "health-elixir", "bee-essence", "health-potion").map { id -> QuestInfo.RewardItem(id, id) }
            // When
            detailView.display(questInfo(gold = 99_999, items = items))
            // Then
            detailView.rewardBottom() shouldBeLessThan detailView.buttonsTop()
        }

    @Test
    fun `hide should not display it`() =
        sharedViewsTest {
            // Given
            val detailView = detailView()
            detailView.display(questInfo())
            // When
            detailView.hide()
            // Then
            detailView.isDetailShown().shouldBeFalse()
        }

    @Test
    fun `QuestDetailView should call its delegate when Back is tapped`() =
        sharedViewsTest {
            // Given
            var backs = 0
            val detailView = detailView()
            detailView.setDelegate(
                object : QuestDetailView.Delegate {
                    override fun questDetailBackSelected() {
                        backs++
                    }
                },
            )
            detailView.display(questInfo())
            addChild(detailView)
            // When
            clickBack(detailView)
            // Then
            backs shouldBe 1
        }
}
