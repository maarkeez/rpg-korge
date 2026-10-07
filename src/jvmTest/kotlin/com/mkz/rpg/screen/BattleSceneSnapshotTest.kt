package com.mkz.rpg.screen

import korlibs.korge.testing.korgeScreenshotTest
import org.junit.jupiter.api.Test

class BattleSceneSnapshotTest {
    @Test
    fun `should render the initial battle layout matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            val actual = capture()
            assertMatchesGolden(INITIAL_LAYOUT, actual)
        }
    }

    @Test
    fun `should render the selected unit state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            selectHumanKnight(scene)
            val actual = capture()
            assertMatchesGolden(UNIT_SELECTED, actual)
        }
    }

    @Test
    fun `should render the selected ability state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            selectHumanKnight(scene)
            selectFirstAbility(scene)
            val actual = capture()
            assertMatchesGolden(ABILITY_SELECTED, actual)
        }
    }
}
