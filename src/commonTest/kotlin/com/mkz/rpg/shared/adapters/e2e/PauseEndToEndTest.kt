package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickQuit
import com.mkz.rpg.screen.adapters.presentation.clickResume
import com.mkz.rpg.screen.adapters.presentation.clickStart
import com.mkz.rpg.screen.adapters.presentation.confirmDialogView
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.pauseView
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.isShown
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PauseEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private suspend fun ViewsForTesting.pause() {
        startNewGame()
        waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
        clickStart(requireNotNull(stage.virtualPadView()))
        waitUntil { stage.pauseView()?.isShown() == true }
    }

    @Test
    fun `should display the pause screen in place of the virtual pad when the human player presses Start while exploring`() =
        sharedViewsTest {
            // When
            pause()
            // Then
            withClue("Expected the pause screen to be displayed") { pauseView()?.isShown()?.shouldBeTrue() }
            withClue("Expected the virtual pad to be hidden while paused") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
        }

    @Test
    fun `should display the virtual pad again when the human player resumes`() =
        sharedViewsTest {
            // Given
            pause()
            // When
            clickResume(requireNotNull(pauseView()))
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true }
            withClue("Expected the virtual pad to be displayed again") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the pause screen to be hidden") { pauseView()?.isShown()?.shouldBeFalse() }
        }

    @Test
    fun `should go back to the menu when the human player quits to the menu and confirms it`() =
        sharedViewsTest {
            // Given
            pause()
            clickQuit(requireNotNull(pauseView()))
            waitUntil { confirmDialogView()?.isShown() == true }
            // When
            clickConfirm(requireNotNull(confirmDialogView()))
            // Then
            waitUntil { menuView() != null }
            withClue("Expected the menu to be displayed") { menuView().shouldNotBeNull() }
        }
}
