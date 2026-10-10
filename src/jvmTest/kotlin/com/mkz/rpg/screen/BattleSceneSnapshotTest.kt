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

    @Test
    fun `should render the inspected enemy state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            selectEnemyRat(scene)
            val actual = capture()
            assertMatchesGolden(ENEMY_INSPECTED, actual)
        }
    }

    @Test
    fun `should render the movement range state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            selectHumanKnight(scene)
            val actual = capture()
            assertMatchesGolden(MOVEMENT_RANGE, actual)
        }
    }

    @Test
    fun `should render the ability cooldowns state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            selectKnightAfterHealing(scene)
            val actual = capture()
            assertMatchesGolden(ABILITY_COOLDOWNS, actual)
        }
    }

    @Test
    fun `should render the cast targets state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            selectKnightAbility(scene, MUSHROOM_ABILITY_INDEX)
            val actual = capture()
            assertMatchesGolden(CAST_TARGETS, actual)
        }
    }

    @Test
    fun `should render the cast preview state matching the golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            previewKnightSkullOnRat(scene)
            val actual = capture()
            assertMatchesGolden(CAST_PREVIEW, actual)
        }
    }
}
