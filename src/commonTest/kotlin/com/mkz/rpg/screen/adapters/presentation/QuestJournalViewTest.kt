package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.completedQuestInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.questInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.task
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestJournalViewTest : ViewsForTesting() {
    private val mainQuest = questInfo(id = "main")
    private val sideQuest = questInfo(id = "side", isMain = false, name = "Bats in the Meadow", tasks = listOf(task(text = "Defeat 3 Bats", progress = 1, count = 3)))
    private val completedQuest = completedQuestInfo().copy(id = "done", isMain = false, name = "The Elder's Errand")
    private val tenQuests = List(10) { index -> questInfo(id = "quest-$index", isMain = false) }

    private fun journalView() = QuestJournalView(BattleSceneResourcesLoader(), TextsMother.texts())

    @Test
    fun `QuestJournalView should be hidden until it displays the quests`() =
        sharedViewsTest {
            journalView().visible.shouldBeFalse()
        }

    @Test
    fun `Display should order the quests as given, each naming its section`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            // When
            journalView.display(listOf(mainQuest, sideQuest, completedQuest))
            // Then
            journalView.displayedRows().shouldContainExactly("main", "side", "done")
            journalView.displayedSections().shouldContainExactly("MAIN QUEST", "SIDE QUEST", "COMPLETED")
            journalView.displayedNames().shouldContainExactly("The Goat Wizard's Lair", "Bats in the Meadow", "The Elder's Errand")
            journalView.displayedTasks().shouldContainExactly("Defeat the Goat Wizard", "Defeat 3 Bats", "Quest completed")
        }

    @Test
    fun `Display should draw the count and the bar only when the task counts above 1`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            // When
            journalView.display(listOf(mainQuest, sideQuest))
            // Then
            journalView.displayedCounts().shouldContainExactly(null, "1/3")
            journalView.isProgressBarShown("main").shouldBeFalse()
            journalView.isProgressBarShown("side").shouldBeTrue()
        }

    @Test
    fun `Display should show the pill when the quest is repeatable`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            // When
            journalView.display(listOf(sideQuest, sideQuest.copy(id = "again", isRepeatable = true)))
            // Then
            journalView.isRepeatablePillShown("side").shouldBeFalse()
            journalView.isRepeatablePillShown("again").shouldBeTrue()
        }

    @Test
    fun `Display should hide the rows out of the list when they do not all fit`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            // When
            journalView.display(tenQuests)
            // Then
            journalView.rowsInSight().shouldContainExactly("quest-0", "quest-1", "quest-2")
        }

    @Test
    fun `Display should keep the scroll when it draws again`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            journalView.display(tenQuests)
            journalView.scrollTo(200.0)
            val inSight = journalView.rowsInSight()
            // When
            journalView.display(tenQuests)
            // Then
            journalView.rowsInSight() shouldBe inSight
            inSight shouldNotContain "quest-0"
        }

    @Test
    fun `Display should keep the list in place when fewer quests are left`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            journalView.display(tenQuests)
            journalView.scrollTo(1000.0)
            // When
            journalView.display(tenQuests.take(2))
            // Then
            journalView.rowsInSight().shouldContainExactly("quest-0", "quest-1")
        }

    @Test
    fun `Drag should scroll the list and not select a row when it moves above the threshold`() =
        sharedViewsTest {
            // Given
            val selected = mutableListOf<String>()
            val journalView = journalView()
            journalView.setDelegate(delegate(selected))
            addChild(journalView)
            journalView.display(tenQuests)
            // When
            dragJournal(journalView, -140.0)
            // Then
            journalView.rowsInSight() shouldNotContain "quest-0"
            selected.shouldBeEmpty()
        }

    @Test
    fun `Tapping a row should call its delegate with the quest when nothing was dragged`() =
        sharedViewsTest {
            // Given
            val selected = mutableListOf<String>()
            val journalView = journalView()
            journalView.setDelegate(delegate(selected))
            addChild(journalView)
            journalView.display(listOf(mainQuest, sideQuest))
            // When
            clickRow(journalView, "side")
            // Then
            selected.shouldContainExactly("side")
        }

    @Test
    fun `Tapping Back should call its delegate when it is displayed`() =
        sharedViewsTest {
            // Given
            val selected = mutableListOf<String>()
            val journalView = journalView()
            journalView.setDelegate(delegate(selected))
            addChild(journalView)
            journalView.display(listOf(mainQuest, sideQuest))
            // When
            clickBack(journalView)
            // Then
            selected.shouldContainExactly("back")
        }

    @Test
    fun `hide should not display it`() =
        sharedViewsTest {
            // Given
            val journalView = journalView()
            journalView.display(listOf(mainQuest, sideQuest))
            // When
            journalView.hide()
            // Then
            journalView.visible.shouldBeFalse()
        }

    private fun delegate(selected: MutableList<String>) =
        object : QuestJournalView.Delegate {
            override fun questJournalSelected(questId: String) {
                selected += questId
            }

            override fun questJournalBackSelected() {
                selected += "back"
            }
        }
}
