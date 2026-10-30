package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickQuit
import com.mkz.rpg.screen.adapters.presentation.clickResume
import com.mkz.rpg.screen.adapters.presentation.clickSave
import com.mkz.rpg.screen.adapters.presentation.clickSlot
import com.mkz.rpg.screen.adapters.presentation.clickStart
import com.mkz.rpg.screen.adapters.presentation.confirmDialogView
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.isSavedShown
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.pauseView
import com.mkz.rpg.screen.adapters.presentation.saveSlotsView
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.isShown
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PauseEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private val storage = InmemoryStorage()

    private suspend fun ViewsForTesting.pause() {
        startNewGame(storage = storage)
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

    @Test
    fun `should save the game into the slot chosen, and display Saved on the pause screen, when the human player saves`() =
        sharedViewsTest {
            // Given
            pause()
            clickSave(requireNotNull(pauseView()))
            waitUntil { saveSlotsView()?.isShown() == true }
            // When
            clickSlot(requireNotNull(saveSlotsView()), 1)
            // Then
            waitUntil { pauseView()?.isSavedShown() == true }
            withClue("Expected Saved on the pause screen") { pauseView()?.isSavedShown()?.shouldBeTrue() }
            withClue("Expected the game saved in slot 1") { storage.keys().shouldContainExactlyInAnyOrder("save.slot1", "save.slot1.summary") }
        }
}
