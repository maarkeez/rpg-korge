package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickBack
import com.mkz.rpg.screen.adapters.presentation.clickCapsule
import com.mkz.rpg.screen.adapters.presentation.displayedTask
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.isDetailShown
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.questDetailView
import com.mkz.rpg.screen.adapters.presentation.questTrackerView
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class QuestEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // A new game, once the scene shows the party and the tracker, the last thing it sets up.
    private suspend fun ViewsForTesting.startExploring() {
        startNewGame()
        waitUntil {
            stage.virtualPadView()?.isPadShown() == true &&
                stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true &&
                stage.questTrackerView()?.displayedTask() != null
        }
    }

    @Test
    fun `should open the detail view in place of the pad when the tracker is tapped, and display the pad again with Back`() =
        sharedViewsTest {
            // Given
            startExploring()
            // When
            clickCapsule(requireNotNull(questTrackerView()))
            waitUntil { questDetailView()?.isDetailShown() == true }
            // Then
            withClue("Expected the detail view to be displayed") { questDetailView()?.isDetailShown()?.shouldBeTrue() }
            withClue("Expected the virtual pad to be hidden while reading the quest") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
            questDetailView()?.displayedTask() shouldBe "Defeat the Goat Wizard"
            // When
            clickBack(requireNotNull(questDetailView()))
            waitUntil { virtualPadView()?.isPadShown() == true }
            // Then
            withClue("Expected the virtual pad to be displayed again") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the detail view to be hidden") { questDetailView()?.isDetailShown()?.shouldBeFalse() }
        }
}
