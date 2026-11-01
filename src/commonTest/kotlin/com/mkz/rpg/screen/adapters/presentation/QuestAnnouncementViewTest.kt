package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.completedQuestInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfoMother.questInfo
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestAnnouncementViewTest : ViewsForTesting() {
    private fun announcementView() = QuestAnnouncementView(BattleSceneResourcesLoader(), TextsMother.texts())

    @Test
    fun `QuestAnnouncementView should be hidden until it announces`() =
        sharedViewsTest {
            // When
            val announcementView = announcementView()
            // Then
            announcementView.isAnnouncementShown().shouldBeFalse()
        }

    @Test
    fun `display should draw the description and the task when the quest started`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            // When
            announcementView.display(Moment.STARTED, questInfo())
            // Then
            announcementView.isAnnouncementShown().shouldBeTrue()
            announcementView.displayedTitle() shouldBe "NEW QUEST"
            announcementView.displayedName() shouldBe "The Goat Wizard's Lair"
            announcementView.displayedDescription() shouldBe "A goat swallowed a wizard's magic and now rules the lair down south."
            announcementView.displayedTask() shouldBe "Defeat the Goat Wizard"
            announcementView.displayedGold().shouldBeEmpty()
            announcementView.isTargetTicked().shouldBeFalse()
        }

    @Test
    fun `display should draw the gold and each item with its name when the quest completed`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            val quest = completedQuestInfo().copy(items = listOf(QuestInfo.RewardItem("hammer", "Hammer"), QuestInfo.RewardItem("health-potion", "Health Potion")))
            // When
            announcementView.display(Moment.COMPLETED, quest)
            // Then
            announcementView.displayedTitle() shouldBe "QUEST COMPLETED"
            announcementView.displayedGold().shouldContainExactly("150 G")
            announcementView.displayedItems().shouldContainExactly("Hammer", "Health Potion")
            announcementView.isTargetTicked().shouldBeTrue()
            announcementView.displayedTask().shouldBeNull()
        }

    @Test
    fun `display should centre the card when it is taller with long texts`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            announcementView.display(Moment.STARTED, questInfo())
            val shortHeight = announcementView.cardHeight()
            val longText = "Hace mucho tiempo una cabra se comió el libro de hechizos de un mago y se tragó toda su magia. Ahora gobierna la guarida del sur, y nadie se atreve a acercarse."
            // When
            announcementView.display(Moment.STARTED, questInfo(name = "La guarida del mago cabra en las montañas del sur", description = longText))
            // Then
            announcementView.cardHeight() shouldBeGreaterThan shortHeight
            val (x, y) = announcementView.cardCentre()
            x shouldBe (QuestAnnouncementView.SCREEN_WIDTH / 2 plusOrMinus 0.01)
            y shouldBe (QuestAnnouncementView.SCREEN_HEIGHT / 2 plusOrMinus 0.01)
        }

    @Test
    fun `hide should not display it`() =
        sharedViewsTest {
            // Given
            val announcementView = announcementView()
            announcementView.display(Moment.STARTED, questInfo())
            // When
            announcementView.hide()
            // Then
            announcementView.isAnnouncementShown().shouldBeFalse()
        }

    @Test
    fun `QuestAnnouncementView should call its delegate when Continue is tapped`() =
        sharedViewsTest {
            // Given
            var continues = 0
            val announcementView = announcementView()
            announcementView.setDelegate(
                object : QuestAnnouncementView.Delegate {
                    override fun questAnnouncementContinued() {
                        continues++
                    }
                },
            )
            announcementView.display(Moment.STARTED, questInfo())
            addChild(announcementView)
            // When
            clickContinue(announcementView)
            // Then
            continues shouldBe 1
        }
}
