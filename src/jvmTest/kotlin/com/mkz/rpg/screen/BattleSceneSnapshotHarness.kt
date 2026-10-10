package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability
import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.ImageDecodingProps
import korlibs.image.format.ImageEncodingProps
import korlibs.image.format.PNG
import korlibs.korge.scene.sceneContainer
import korlibs.korge.testing.BitmapComparer
import korlibs.korge.testing.OffscreenStage
import korlibs.korge.testing.simulateRenderFrame
import korlibs.korge.ui.UIButton
import korlibs.korge.view.View
import korlibs.korge.view.descendantsWith
import korlibs.math.geom.Size
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.io.File

internal const val SNAPSHOT_SEED: Long = 42L
internal val SNAPSHOT_WINDOW_SIZE: Size = Size(390.0, 844.0)
internal const val SNAPSHOT_GOLDEN_DIR: String = "src/jvmTest/resources/snapshots"
internal const val SNAPSHOT_REPORT_DIR: String = "build/reports/snapshots"

internal const val INITIAL_LAYOUT: String = "battle-initial-layout"
internal const val UNIT_SELECTED: String = "battle-unit-selected"
internal const val ABILITY_SELECTED: String = "battle-ability-selected"
internal const val CAST_TARGETS: String = "battle-cast-targets"
internal const val CAST_PREVIEW: String = "battle-cast-preview"
internal const val ENEMY_INSPECTED: String = "battle-enemy-inspected"
internal const val MOVEMENT_RANGE: String = "battle-movement-range"
internal const val ABILITY_COOLDOWNS: String = "battle-ability-cooldowns"
internal const val SHOWCASE_SCENARIO: String = "scenarios/ui-showcase.json"

private const val HUMAN_KNIGHT_TILE = "row-6-column-6"
private const val HEAL_ABILITY_INDEX = 5
internal const val MUSHROOM_ABILITY_INDEX = 1
internal const val SKULL_ABILITY_INDEX = 2
private const val ENEMY_RAT_A_TILE = "row-6-column-7"
private const val SELECTION_VIEW = BattlefieldView.SELECTION
private const val ENEMY_RAT_TILE = "row-6-column-7"
private const val EXPECTED_UNIT_COUNT = 4
private const val READY_TIMEOUT_MS = 10_000L
private const val POLL_DELAY_MS = 10L
private const val SETTLE_DELAY_MS = 200L

internal suspend fun OffscreenStage.createBattleScene(scenarioPath: String? = null): BattleScene {
    val container = sceneContainer()
    return container.changeTo { BattleScene(seed = SNAPSHOT_SEED, scenarioPath = scenarioPath) }
}

internal suspend fun awaitBattleReady(scene: BattleScene) {
    awaitUntil { namedViewCount(scene.battlefieldView, BattlefieldView.BATTLE_UNIT) >= EXPECTED_UNIT_COUNT }
}

internal suspend fun OffscreenStage.selectHumanKnight(scene: BattleScene) {
    clickView(scene.battlefieldView, HUMAN_KNIGHT_TILE)
    awaitUntil { scene.battleHudView.children.isNotEmpty() }
}

internal suspend fun OffscreenStage.selectEnemyRat(scene: BattleScene) {
    clickView(scene.battlefieldView, ENEMY_RAT_TILE)
    awaitUntil { scene.battleHudView.children.isNotEmpty() }
}

internal suspend fun OffscreenStage.selectFirstAbility(scene: BattleScene) {
    val abilityButton =
        scene.battleUnitInfoView
            .descendantsWith { it is AbilityButtonView && it.visible }
            .first() as UIButton
    abilityButton.simulateClick(views)
    awaitUntil { namedViewCount(scene.battleUnitInfoView, AbilityButtonView.ABILITY_SELECTION) == 1 }
}

internal suspend fun OffscreenStage.selectKnightAbility(
    scene: BattleScene,
    abilityIndex: Int,
) {
    selectHumanKnight(scene)
    val button = scene.battleUnitInfoView.descendantsWith { it is AbilityButtonView && it.visible }[abilityIndex] as UIButton
    button.simulateClick(views)
    awaitUntil { namedViewCount(scene.battleUnitInfoView, AbilityButtonView.ABILITY_SELECTION) == 1 }
}

/** Selects the knight's skull and taps rat A, so the cast preview is displayed. */
internal suspend fun OffscreenStage.previewKnightSkullOnRat(scene: BattleScene) {
    selectKnightAbility(scene, SKULL_ABILITY_INDEX)
    clickView(scene.battlefieldView, ENEMY_RAT_A_TILE)
    awaitUntil { namedViewCount(scene.battlefieldView, SELECTION_VIEW) > 0 && scene.attackPreviewView.displayedLines.isNotEmpty() }
}

/** Casts the knight's heal on itself, passes the turn and reselects the knight, so the heal slot shows its cooldown. */
internal suspend fun OffscreenStage.selectKnightAfterHealing(scene: BattleScene) {
    selectHumanKnight(scene)
    val healButton = scene.battleUnitInfoView.descendantsWith { it is AbilityButtonView && it.visible }[HEAL_ABILITY_INDEX] as UIButton
    healButton.simulateClick(views)
    awaitUntil { namedViewCount(scene.battleUnitInfoView, AbilityButtonView.ABILITY_SELECTION) == 1 }
    clickView(scene.battlefieldView, HUMAN_KNIGHT_TILE)
    clickActionButton(scene, "Confirm")
    clickActionButton(scene, "Finish turn")
    delay(SETTLE_DELAY_MS)
    selectHumanKnight(scene)
    awaitUntil {
        val slots = scene.battleUnitInfoView.abilitySlots
        slots[HEAL_ABILITY_INDEX].status == SearchAbilityAvailability.AbilityAvailability.Status.COOLDOWN
    }
    delay(SETTLE_DELAY_MS)
}

private suspend fun OffscreenStage.clickActionButton(
    scene: BattleScene,
    text: String,
) {
    awaitUntil { scene.playerCallToActionView.descendantsWith { it is UIButton && it.text == text }.isNotEmpty() }
    (scene.playerCallToActionView.descendantsWith { it is UIButton && it.text == text }.first() as UIButton).simulateClick(views)
    delay(POLL_DELAY_MS)
}

internal suspend fun OffscreenStage.capture(): Bitmap32 {
    delay(SETTLE_DELAY_MS)
    return simulateRenderFrame()
}

internal fun loadGolden(name: String): Bitmap32? {
    val file = File(SNAPSHOT_GOLDEN_DIR, "$name.png")
    if (!file.exists()) return null
    return PNG.decode(file.readBytes(), ImageDecodingProps.DEFAULT_STRAIGHT).toBMP32()
}

internal fun writeGolden(
    name: String,
    bitmap: Bitmap32,
) {
    val file = File(SNAPSHOT_GOLDEN_DIR, "$name.png")
    file.parentFile?.mkdirs()
    file.writeBytes(PNG.encode(bitmap, ImageEncodingProps(quality = 1.0)))
}

internal fun writeReport(
    name: String,
    actual: Bitmap32,
    golden: Bitmap32?,
) {
    val reportDir = File(SNAPSHOT_REPORT_DIR).apply { mkdirs() }
    File(reportDir, "$name.actual.png").writeBytes(PNG.encode(actual, ImageEncodingProps(quality = 1.0)))
    if (golden != null) {
        File(reportDir, "$name.golden.png").writeBytes(PNG.encode(golden, ImageEncodingProps(quality = 1.0)))
    }
}

internal fun assertMatchesGolden(
    name: String,
    actual: Bitmap32,
) {
    val golden = loadGolden(name)
    if (golden == null) {
        writeReport(name, actual, null)
        throw AssertionError(
            "Golden image for '$name' not found at ${File(SNAPSHOT_GOLDEN_DIR, "$name.png").absolutePath}. " +
                "Run './gradlew updateSnapshots' to generate it.",
        )
    }
    val result = BitmapComparer.compare(golden, actual)
    if (result.pixelDiffCount != 0) {
        writeReport(name, actual, golden)
        throw AssertionError(
            "Snapshot '$name' differs from the golden image: ${result.pixelDiffCount} pixels differ " +
                "(max pixel distance ${result.pixelMaxDistance}, psnr ${result.psnr}). " +
                "Diff images were written to ${File(SNAPSHOT_REPORT_DIR).absolutePath}. " +
                "If the visual change is intentional, run './gradlew updateSnapshots' to accept it.",
        )
    }
}

private suspend fun awaitUntil(condition: () -> Boolean) {
    withTimeout(READY_TIMEOUT_MS) {
        while (!condition()) {
            delay(POLL_DELAY_MS)
        }
    }
}

private fun OffscreenStage.clickView(
    root: View,
    name: String,
) {
    val view = root.descendantsWith { it.name == name }.first() as UIButton
    view.simulateClick(views)
}

private fun namedViewCount(
    root: View,
    name: String,
): Int = root.descendantsWith { it.name == name }.size
