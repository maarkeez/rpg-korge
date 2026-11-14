package com.mkz.tessavar.shared.adapters.e2e

import com.mkz.tessavar.screen.adapters.presentation.MenuView
import com.mkz.tessavar.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.tessavar.screen.adapters.presentation.battlefieldView
import com.mkz.tessavar.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.tessavar.screen.adapters.presentation.clickArrow
import com.mkz.tessavar.screen.adapters.presentation.clickBuildWorld
import com.mkz.tessavar.screen.adapters.presentation.clickDecline
import com.mkz.tessavar.screen.adapters.presentation.clickPlayNewGame
import com.mkz.tessavar.screen.adapters.presentation.consentView
import com.mkz.tessavar.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.tessavar.screen.adapters.presentation.isBattlefieldDisplayed
import com.mkz.tessavar.screen.adapters.presentation.isConsentShown
import com.mkz.tessavar.screen.adapters.presentation.isPadShown
import com.mkz.tessavar.screen.adapters.presentation.menuView
import com.mkz.tessavar.screen.adapters.presentation.paletteTerrainIds
import com.mkz.tessavar.screen.adapters.presentation.virtualPadView
import com.mkz.tessavar.screen.adapters.presentation.worldBuilderHudView
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.startApp
import korlibs.korge.service.storage.IStorage
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private val WAIT_TIMEOUT = 10.seconds

suspend fun ViewsForTesting.waitUntil(
    timeout: Duration = WAIT_TIMEOUT,
    condition: () -> Boolean,
) {
    val start = TimeSource.Monotonic.markNow()
    while (!condition()) {
        check(start.elapsedNow() < timeout) { "waitUntil timed out after $timeout" }
        delay(frameTime)
    }
}

suspend fun ViewsForTesting.openMenu(): MenuView {
    waitUntil { stage.menuView() != null }
    stage.consentView()?.takeIf { consentView -> consentView.isConsentShown() }?.let { consentView -> clickDecline(consentView) }
    return requireNotNull(stage.menuView()) { "The menu is not displayed" }
}

suspend fun ViewsForTesting.startNewGame(
    random: Random = FixedDoublesRandom.cpuBattleUnitsAlwaysResting(),
    storage: IStorage = InmemoryStorage(),
) {
    stage.startApp(random = random, storage = storage)
    clickPlayNewGame(openMenu())
    waitUntil { stage.virtualPadView()?.isPadShown() == true && stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }
}

private data class RouteStep(
    val direction: Direction,
    val row: Int,
    val column: Int,
)

const val KNIGHT_START_ROW = 2
const val KNIGHT_START_COLUMN = 1

private val ROUTE_TO_BATTLE =
    listOf(
        RouteStep(Direction.EAST, row = 2, column = 2),
        RouteStep(Direction.EAST, row = 2, column = 3),
        RouteStep(Direction.EAST, row = 2, column = 4),
        RouteStep(Direction.EAST, row = 2, column = 5),
        RouteStep(Direction.EAST, row = 2, column = 6),
        RouteStep(Direction.EAST, row = 2, column = 7),
    )

const val LEADER_ROW_IN_BATTLE = 2
const val LEADER_COLUMN_IN_BATTLE = 7

const val BAT_ROW_IN_BATTLE = 2
const val BAT_COLUMN_IN_BATTLE = 8

suspend fun ViewsForTesting.startNewBattle() {
    startNewGame()
    ROUTE_TO_BATTLE.forEach { step -> walkLeader(step) }
    waitUntil { stage.canHumanPlayerAct() }
}

private suspend fun ViewsForTesting.walkLeader(step: RouteStep) {
    val virtualPadView = requireNotNull(stage.virtualPadView()) { "The virtual pad is not displayed" }
    val battlefieldView = requireNotNull(stage.battlefieldView()) { "The battlefield is not displayed" }
    clickArrow(virtualPadView, step.direction)
    waitUntil { battlefieldView.hasBattleUnitAt(step.row, step.column) }
    require(battlefieldView.hasBattleUnitAt(step.row, step.column)) { "The leader did not walk to (${step.row},${step.column})" }
}

suspend fun ViewsForTesting.startWorldBuilder() {
    stage.startApp(storage = InmemoryStorage())
    clickBuildWorld(openMenu())
    waitUntil { stage.battlefieldView()?.isBattlefieldDisplayed() == true && stage.worldBuilderHudView()?.paletteTerrainIds()?.isNotEmpty() == true }
}

suspend fun ViewsForTesting.waitFor(duration: Duration) {
    repeat((duration / frameTime).toInt()) { delay(frameTime) }
}
