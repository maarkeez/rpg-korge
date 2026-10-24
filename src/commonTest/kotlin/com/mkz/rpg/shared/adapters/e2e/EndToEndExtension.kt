package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
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
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.startGame
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration
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

/**
 * Starts the game as the player does, and plays a new game from the menu. By default, the CPU battle units always rest
 * instead of wandering, so they stand where the battle setup deploys them.
 */
suspend fun ViewsForTesting.startNewGame(random: Random = FixedDoublesRandom(0.0)) {
    stage.startGame(random = random)
    waitUntil { stage.menuView() != null }
    clickPlayNewGame(requireNotNull(stage.menuView()) { "The menu is not displayed" })
}

/** A leader step of the route to the battle: the arrow pressed, and the tile the leader walks to. */
private data class RouteStep(
    val direction: DirectionDto,
    val row: Int,
    val column: Int,
)

/** The battle setup deploys the knight, alone in the party, at the west end of the start corridor (row 2, 1 tile wide). */
const val KNIGHT_START_ROW = 2
const val KNIGHT_START_COLUMN = 1

/**
 * Walking east four times leaves the knight at (2,5), where the CPU bat at (2,10), further along the corridor, has it
 * within reach: 5 tiles. No tile walked before is within reach of a CPU battle unit. The bat engages and plays first,
 * alone: it walks to (2,6), next to the knight, and the other CPU battle units stay out of the battle.
 */
private val ROUTE_TO_BATTLE =
    listOf(
        RouteStep(DirectionDto.EAST, row = 2, column = 2),
        RouteStep(DirectionDto.EAST, row = 2, column = 3),
        RouteStep(DirectionDto.EAST, row = 2, column = 4),
        RouteStep(DirectionDto.EAST, row = 2, column = 5),
    )

/** Where the knight is once [startNewBattle] has walked it to the battle. */
const val LEADER_ROW_IN_BATTLE = 2
const val LEADER_COLUMN_IN_BATTLE = 5

/** Where the CPU bat stands once it has engaged the knight, next to it in the corridor. */
const val BAT_ROW_IN_BATTLE = 2
const val BAT_COLUMN_IN_BATTLE = 6

/**
 * Plays a new game and walks the party with the virtual pad until the CPU engages, as the player does. Returns once
 * the human player can act on their first turn, after the CPU one.
 */
suspend fun ViewsForTesting.startNewBattle() {
    startNewGame()
    waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
    ROUTE_TO_BATTLE.forEach { step -> walkLeader(step) }
    waitUntil { stage.canHumanPlayerAct() }
}

/**
 * Clicks the arrow once, and waits until the leader is displayed on the tile. A click made while the scene plays the
 * previous steps is kept until it is idle, and the leader glides off the tiles meanwhile, so clicking again would walk
 * it further.
 */
private suspend fun ViewsForTesting.walkLeader(step: RouteStep) {
    val virtualPadView = requireNotNull(stage.virtualPadView()) { "The virtual pad is not displayed" }
    val battlefieldView = requireNotNull(stage.battlefieldView()) { "The battlefield is not displayed" }
    clickArrow(virtualPadView, step.direction)
    waitUntil { battlefieldView.hasBattleUnitAt(step.row, step.column) }
    require(battlefieldView.hasBattleUnitAt(step.row, step.column)) { "The leader did not walk to (${step.row},${step.column})" }
}

/** Starts the game as the player does, opens the map builder from the menu and waits until the map is displayed. */
suspend fun ViewsForTesting.startMapBuilder() {
    stage.startGame()
    waitUntil { stage.menuView() != null }
    clickBuildMap(requireNotNull(stage.menuView()) { "The menu is not displayed" })
    waitUntil { stage.battlefieldView()?.isBattlefieldDisplayed() == true && stage.mapBuilderHudView()?.paletteTerrainIds()?.isNotEmpty() == true }
}

/** Lets the game run for the given game time, frame by frame. */
suspend fun ViewsForTesting.waitFor(duration: Duration) {
    repeat((duration / frameTime).toInt()) { delay(frameTime) }
}
