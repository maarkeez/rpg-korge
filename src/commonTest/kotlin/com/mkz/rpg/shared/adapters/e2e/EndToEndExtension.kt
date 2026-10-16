package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickBuildMap
import com.mkz.rpg.screen.adapters.presentation.clickPlayNewGame
import com.mkz.rpg.screen.adapters.presentation.isBattlefieldDisplayed
import com.mkz.rpg.screen.adapters.presentation.mapBuilderHudView
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.paletteTerrainIds
import com.mkz.rpg.startGame
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// Shared helpers for the end-to-end tests.

private val WAIT_TIMEOUT = 10.seconds

/**
 * Waits until the condition is met, or the timeout is reached.
 *
 * The game loads its resources asynchronously in real time, while the frames are simulated much faster than that.
 * Hence, waiting is bounded by real time instead of frames. Each check waits for the next frame, so the game runs in between.
 */
suspend fun ViewsForTesting.waitUntil(condition: () -> Boolean) {
    val start = TimeSource.Monotonic.markNow()
    while (!condition() && start.elapsedNow() < WAIT_TIMEOUT) {
        delay(frameTime)
    }
}

/** Starts the game as the player does, and plays a new game from the menu. */
suspend fun ViewsForTesting.startNewGame() {
    stage.startGame()
    waitUntil { stage.menuView() != null }
    clickPlayNewGame(requireNotNull(stage.menuView()) { "The menu is not displayed" })
}

/** Starts the game as the player does, opens the map builder from the menu and waits until the map is displayed. */
suspend fun ViewsForTesting.startMapBuilder() {
    stage.startGame()
    waitUntil { stage.menuView() != null }
    clickBuildMap(requireNotNull(stage.menuView()) { "The menu is not displayed" })
    waitUntil { stage.battlefieldView()?.isBattlefieldDisplayed() == true && stage.mapBuilderHudView()?.paletteTerrainIds()?.isNotEmpty() == true }
}
