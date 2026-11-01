package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestMarkerViewTest : ViewsForTesting() {
    @Test
    fun `QuestMarkerView should draw the sign of a quest to start in gold`() =
        sharedViewsTest {
            QuestMarkerView(QuestMarkerView.Sign.QUEST_TO_START).displayedColor() shouldBe HudStyle.SELECTION
        }

    @Test
    fun `QuestMarkerView should draw the sign of a talk task in green`() =
        sharedViewsTest {
            QuestMarkerView(QuestMarkerView.Sign.TALK_TASK).displayedColor() shouldBe HudStyle.ALLY
        }
}
