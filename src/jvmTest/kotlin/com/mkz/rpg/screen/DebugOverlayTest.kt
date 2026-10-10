package com.mkz.rpg.screen

import korlibs.event.Key
import korlibs.korge.scene.SceneContainer
import korlibs.korge.scene.sceneContainer
import korlibs.korge.testing.OffscreenStage
import korlibs.korge.testing.korgeScreenshotTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val DEBUG_SEED: Long = 42L
private const val KEY_HOLD_MS: Long = 150L
private const val KEY_SETTLE_MS: Long = 150L
private const val READY_TIMEOUT_MS: Long = 10_000L
private const val POLL_DELAY_MS: Long = 10L

class DebugOverlayTest {
    @Test
    fun `should show the debug overlay when F3 is pressed and debug is enabled`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            // Given
            val scene = createDebugScene()
            awaitBattleReady(scene)
            val overlay = scene.overlayOf()
            assertThat(overlay.visible).isFalse()
            assertThat(overlay.label.text).isEmpty()

            // When
            scene.pressKey(Key.F3)

            // Then
            awaitUntil { overlay.visible }
            assertThat(overlay.label.text).contains("seed $DEBUG_SEED")
        }
    }

    @Test
    fun `should hide the debug overlay when F3 is pressed again`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            // Given
            val scene = createDebugScene()
            awaitBattleReady(scene)
            val overlay = scene.overlayOf()
            scene.pressKey(Key.F3)
            awaitUntil { overlay.visible }

            // When
            scene.pressKey(Key.F3)

            // Then
            awaitUntil { !overlay.visible }
            assertThat(overlay.visible).isFalse()
        }
    }

    @Test
    fun `should display the round and current player when the overlay is shown`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            // Given
            val scene = createDebugScene()
            awaitBattleReady(scene)
            val overlay = scene.overlayOf()

            // When
            scene.pressKey(Key.F3)

            // Then
            awaitUntil { overlay.label.text.contains("round 1") }
            assertThat(overlay.label.text).contains("round 1 | Human")
        }
    }

    @Test
    fun `should not install the debug overlay when debug is disabled`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            // Given
            val scene = createDisabledScene()
            awaitBattleReady(scene)

            // When
            scene.pressKey(Key.F3)
            delay(KEY_HOLD_MS)

            // Then
            assertThat(scene.sceneView.children.none { it is DebugOverlay }).isTrue()
        }
    }

    @Test
    fun `should restart the battle with the same seed when R is pressed`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            // Given
            val container = sceneContainer()
            val scene = container.changeTo { BattleScene(seed = DEBUG_SEED, debugEnabled = true) }
            awaitBattleReady(scene)
            val overlay = scene.overlayOf()
            scene.pressKey(Key.F3)
            awaitUntil { overlay.visible }

            // When
            scene.pressKey(Key.R)

            // Then
            val newScene = awaitSceneChange(container, scene)
            assertThat(newScene.seed).isEqualTo(DEBUG_SEED)
        }
    }

    @Test
    fun `should restart the battle with the next seed when N is pressed`() {
        korgeScreenshotTest(
            windowSize = SNAPSHOT_WINDOW_SIZE,
            virtualSize = SNAPSHOT_WINDOW_SIZE,
        ) {
            // Given
            val container = sceneContainer()
            val scene = container.changeTo { BattleScene(seed = DEBUG_SEED, debugEnabled = true) }
            awaitBattleReady(scene)
            val overlay = scene.overlayOf()
            scene.pressKey(Key.F3)
            awaitUntil { overlay.visible }

            // When
            scene.pressKey(Key.N)

            // Then
            val newScene = awaitSceneChange(container, scene)
            assertThat(newScene.seed).isEqualTo(DEBUG_SEED + 1)
        }
    }
}

private suspend fun OffscreenStage.createDebugScene(): BattleScene {
    val container = sceneContainer()
    return container.changeTo { BattleScene(seed = DEBUG_SEED, debugEnabled = true) }
}

private suspend fun OffscreenStage.createDisabledScene(): BattleScene {
    val container = sceneContainer()
    return container.changeTo { BattleScene(seed = DEBUG_SEED) }
}

private suspend fun BattleScene.pressKey(key: Key) {
    stage.keys.triggerKeyEvent(key, up = false)
    delay(KEY_HOLD_MS)
    stage.keys.triggerKeyEvent(key, up = true)
    delay(KEY_SETTLE_MS)
}

private fun BattleScene.overlayOf(): DebugOverlay = sceneView.children.filterIsInstance<DebugOverlay>().first()

private suspend fun awaitSceneChange(
    container: SceneContainer,
    oldScene: BattleScene,
): BattleScene {
    var result: BattleScene? = null
    awaitUntil {
        val current = container.currentScene
        if (current is BattleScene && current !== oldScene) {
            result = current
            true
        } else {
            false
        }
    }
    return result!!
}

private suspend fun awaitUntil(condition: () -> Boolean) {
    withTimeout(READY_TIMEOUT_MS) {
        while (!condition()) {
            delay(POLL_DELAY_MS)
        }
    }
}
