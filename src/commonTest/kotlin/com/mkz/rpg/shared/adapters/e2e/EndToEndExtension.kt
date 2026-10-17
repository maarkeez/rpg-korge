package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.rpg.screen.adapters.presentation.clickArrow
import com.mkz.rpg.screen.adapters.presentation.clickBuildMap
import com.mkz.rpg.screen.adapters.presentation.clickPlayNewGame
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.isBattlefieldDisplayed
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.mapBuilderHudView
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.paletteTerrainIds
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
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

/** A leader step of the route to the battle: the arrow pressed, and the tile the leader walks to. */
private data class RouteStep(
    val direction: DirectionDto,
    val row: Int,
    val column: Int,
)

/**
 * The battle setup deploys the knights at (6,6), which leads, and (7,6). Walking north twice and west three times
 * leaves the leader at (4,3) and the other knight at (4,4), where the CPU rat at (1,1) has the leader within reach: 5
 * tiles. No tile walked before is within reach of a CPU battle unit.
 */
private val ROUTE_TO_BATTLE =
    listOf(
        RouteStep(DirectionDto.NORTH, row = 5, column = 6),
        RouteStep(DirectionDto.NORTH, row = 4, column = 6),
        RouteStep(DirectionDto.WEST, row = 4, column = 5),
        RouteStep(DirectionDto.WEST, row = 4, column = 4),
        RouteStep(DirectionDto.WEST, row = 4, column = 3),
    )

/** Where the leading knight is once [startNewBattle] has walked it to the battle. */
const val LEADER_ROW_IN_BATTLE = 4
const val LEADER_COLUMN_IN_BATTLE = 3

/**
 * Plays a new game and walks the party with the virtual pad until the CPU engages, as the player does. Returns once
 * the human player can act on the first turn of the battle.
 */
suspend fun ViewsForTesting.startNewBattle() {
    startNewGame()
    waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(6, 6) == true }
    ROUTE_TO_BATTLE.forEach { step -> walkLeader(step) }
    waitUntil { stage.canHumanPlayerAct() }
}

/**
 * Clicks the arrow until the leader is displayed on the tile, as a click is ignored while the scene plays the previous
 * steps. Once the leader is displayed there, no further click is made, so it walks a single tile.
 */
private suspend fun ViewsForTesting.walkLeader(step: RouteStep) {
    val virtualPadView = requireNotNull(stage.virtualPadView()) { "The virtual pad is not displayed" }
    val battlefieldView = requireNotNull(stage.battlefieldView()) { "The battlefield is not displayed" }
    val start = TimeSource.Monotonic.markNow()
    while (!battlefieldView.hasBattleUnitAt(step.row, step.column) && start.elapsedNow() < WAIT_TIMEOUT) {
        clickArrow(virtualPadView, step.direction)
        delay(frameTime)
    }
    require(battlefieldView.hasBattleUnitAt(step.row, step.column)) { "The leader did not walk to (${step.row},${step.column})" }
}

/** Starts the game as the player does, opens the map builder from the menu and waits until the map is displayed. */
suspend fun ViewsForTesting.startMapBuilder() {
    stage.startGame()
    waitUntil { stage.menuView() != null }
    clickBuildMap(requireNotNull(stage.menuView()) { "The menu is not displayed" })
    waitUntil { stage.battlefieldView()?.isBattlefieldDisplayed() == true && stage.mapBuilderHudView()?.paletteTerrainIds()?.isNotEmpty() == true }
}
