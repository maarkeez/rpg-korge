package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickArrow
import com.mkz.rpg.screen.adapters.presentation.clickConfirm
import com.mkz.rpg.screen.adapters.presentation.clickLoadGame
import com.mkz.rpg.screen.adapters.presentation.clickQuit
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
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class SaveAndLoadEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // Two steps east of where the battle setup deploys the hero: out of reach of the CPU battle units.
    private val savedRow = KNIGHT_START_ROW
    private val savedColumn = KNIGHT_START_COLUMN + 2

    private val storage = InmemoryStorage()

    @Test
    fun `Game should be back where it was saved when the slot is loaded after quitting to the menu`() =
        sharedViewsTest {
            // Given: the hero walks two steps east, and the game is saved in slot 1.
            startNewGame(storage = storage)
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
            repeat(2) { step ->
                clickArrow(requireNotNull(virtualPadView()), Direction.EAST)
                waitUntil { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN + step + 1) == true && virtualPadView()?.isPadShown() == true }
            }
            clickStart(requireNotNull(virtualPadView()))
            waitUntil { pauseView()?.isShown() == true }
            clickSave(requireNotNull(pauseView()))
            waitUntil { saveSlotsView()?.isShown() == true }
            clickSlot(requireNotNull(saveSlotsView()), 1)
            waitUntil { pauseView()?.isSavedShown() == true }
            // And the human player quits to the menu.
            clickQuit(requireNotNull(pauseView()))
            waitUntil { confirmDialogView()?.isShown() == true }
            clickConfirm(requireNotNull(confirmDialogView()))
            waitUntil { menuView()?.isShown() == true }
            // When
            clickLoadGame(requireNotNull(menuView()))
            waitUntil { saveSlotsView()?.isShown() == true }
            clickSlot(requireNotNull(saveSlotsView()), 1)
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(savedRow, savedColumn) == true }
            withClue("Expected the hero where the game was saved") { battlefieldView()?.hasBattleUnitAt(savedRow, savedColumn)?.shouldBeTrue() }
            withClue("Expected nobody where the hero starts a new game") { battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN)?.shouldBeFalse() }
            withClue("Expected the virtual pad, as the game is loaded exploring") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
        }
}
