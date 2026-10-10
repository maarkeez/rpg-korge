package com.mkz.rpg.screen

import com.mkz.rpg.screen.feedback.FeedbackTiming
import korlibs.korge.testing.korgeScreenshotTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("update-snapshots")
class BattleSceneSnapshotUpdateTest {
    @Test
    fun `should write the initial battle layout golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            writeGolden(INITIAL_LAYOUT, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$INITIAL_LAYOUT.png")).exists()
    }

    @Test
    fun `should write the selected unit state golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            selectHumanKnight(scene)
            writeGolden(UNIT_SELECTED, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$UNIT_SELECTED.png")).exists()
    }

    @Test
    fun `should write the selected ability state golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            selectHumanKnight(scene)
            selectFirstAbility(scene)
            writeGolden(ABILITY_SELECTED, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$ABILITY_SELECTED.png")).exists()
    }

    @Test
    fun `should write the inspected enemy state golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            selectEnemyRat(scene)
            writeGolden(ENEMY_INSPECTED, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$ENEMY_INSPECTED.png")).exists()
    }

    @Test
    fun `should write the movement range state golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            selectHumanKnight(scene)
            writeGolden(MOVEMENT_RANGE, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$MOVEMENT_RANGE.png")).exists()
    }

    @Test
    fun `should write the ability cooldowns golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene()
            awaitBattleReady(scene)
            selectKnightAfterHealing(scene)
            writeGolden(ABILITY_COOLDOWNS, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$ABILITY_COOLDOWNS.png")).exists()
    }

    @Test
    fun `should write the cast targets golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            selectKnightAbility(scene, MUSHROOM_ABILITY_INDEX)
            writeGolden(CAST_TARGETS, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$CAST_TARGETS.png")).exists()
    }

    @Test
    fun `should write the cast preview golden image`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO)
            awaitBattleReady(scene)
            previewKnightSkullOnRat(scene)
            writeGolden(CAST_PREVIEW, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$CAST_PREVIEW.png")).exists()
    }

    @Test
    fun `should write the golden image of the battle after the cpu turn has played back`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            val scene = createBattleScene(scenarioPath = SHOWCASE_SCENARIO, feedbackTiming = FeedbackTiming.Standard)
            awaitBattleReady(scene)
            finishTurnAndAwaitPlayback(scene)
            writeGolden(AFTER_CPU_PLAYBACK, capture())
        }
        assertThat(File(SNAPSHOT_GOLDEN_DIR, "$AFTER_CPU_PLAYBACK.png")).exists()
    }
}
