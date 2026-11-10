package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.completedQuestInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.questInfo
import com.mkz.rpg.shared.adapters.e2e.waitFor
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.io.async.launchImmediately
import korlibs.korge.tests.ViewsForTesting
import korlibs.time.milliseconds
import kotlin.test.Test

class QuestAnnouncementViewTest : ViewsForTesting() {
    private fun announcementView() = QuestAnnouncementView(GameSceneResourcesLoader(), MessagesMother.messages())

    @Test
    fun `QuestAnnouncementView should be hidden until it announces`() =
        sharedViewsTest {
            // When
            val announcementView = announcementView()
            // Then
            announcementView.isAnnouncementShown().shouldBeFalse()
        }

    @Test
    fun `display should draw the task when the quest started`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            // When
            announcementView.display(Moment.STARTED, questInfo())
            // Then
            announcementView.isAnnouncementShown().shouldBeTrue()
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "The Goat Wizard's Lair"
            announcementView.displayedTask() shouldBe "Defeat the Goat Wizard"
            announcementView.displayedGold().shouldBeEmpty()
            announcementView.isTargetTicked().shouldBeFalse()
        }

    @Test
    fun `display should draw the gold and a slot per item when the quest completed`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            val quest = completedQuestInfo().copy(items = listOf(QuestInfo.RewardItem("hammer", "Hammer"), QuestInfo.RewardItem("health-potion", "Health Potion")))
            // When
            announcementView.display(Moment.COMPLETED, quest)
            // Then
            announcementView.displayedTitle() shouldBe "QUEST COMPLETED"
            announcementView.displayedGold().shouldContainExactly("150 G")
            announcementView.displayedItemSlots() shouldBe 2
            announcementView.isTargetTicked().shouldBeTrue()
            announcementView.displayedTask().shouldBeNull()
        }

    @Test
    fun `display should cut the name when it does not fit the announcement`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            // When
            announcementView.display(Moment.STARTED, questInfo(name = "La guarida del mago cabra en las montañas del sur, donde nadie se atreve a acercarse"))
            // Then
            announcementView.displayedName()?.endsWith("…")?.shouldBeTrue()
        }

    @Test
    fun `Announce should hide the announcement when it has been held and faded`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            addChild(announcementView)
            // When
            launchImmediately { announcementView.announce(Moment.STARTED, questInfo()) }
            // Then
            announcementView.isAnnouncementShown().shouldBeTrue()
            // When
            waitFor(QuestAnnouncementView.HOLD_DURATION - 100.milliseconds)
            // Then
            announcementView.isAnnouncementShown().shouldBeTrue()
            announcementView.alpha shouldBe 1.0
            // When
            waitFor(QuestAnnouncementView.FADE_DURATION + 300.milliseconds)
            // Then
            announcementView.isAnnouncementShown().shouldBeFalse()
        }

    @Test
    fun `Announce should show the announcement fully when it announces again after fading`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            addChild(announcementView)
            announcementView.announce(Moment.STARTED, questInfo())
            // When
            announcementView.display(Moment.STARTED, questInfo())
            // Then
            announcementView.isAnnouncementShown().shouldBeTrue()
            announcementView.alpha shouldBe 1.0
        }

    @Test
    fun `Announce should take no taps when it is displayed`() =
        sharedViewsTest {
            // Given
            var confirms = 0
            val button = buttonUnderTheAnnouncement { confirms++ }
            val announcementView = announcementView()
            addChild(announcementView)
            announcementView.display(Moment.COMPLETED, completedQuestInfo())
            // When
            clickConfirm(button)
            // Then
            confirms shouldBe 1
        }
}
