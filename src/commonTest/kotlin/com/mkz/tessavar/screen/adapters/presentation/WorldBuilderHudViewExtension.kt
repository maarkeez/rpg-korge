package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.isFaded
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

fun WorldBuilderHudView.rowsText(): String = rowsValue.text

fun WorldBuilderHudView.columnsText(): String = columnsValue.text

fun WorldBuilderHudView.enabledRowsSteps(): List<Int> = rowsStepButtons.filterValues { button -> button.enabled }.keys.sorted()

fun WorldBuilderHudView.enabledColumnsSteps(): List<Int> = columnsStepButtons.filterValues { button -> button.enabled }.keys.sorted()

fun WorldBuilderHudView.fadedRowsSteps(): List<Int> = rowsStepButtons.filterValues { button -> button.isFaded() }.keys.sorted()

fun WorldBuilderHudView.fadedColumnsSteps(): List<Int> = columnsStepButtons.filterValues { button -> button.isFaded() }.keys.sorted()

suspend fun ViewsForTesting.clickRowsStep(
    hudView: WorldBuilderHudView,
    step: Int,
) = clickOn(hudView.rowsStepButtons.getValue(step))

suspend fun ViewsForTesting.clickColumnsStep(
    hudView: WorldBuilderHudView,
    step: Int,
) = clickOn(hudView.columnsStepButtons.getValue(step))

fun WorldBuilderHudView.paletteTerrainIds(): List<String> = terrainButtons.keys.toList()

fun WorldBuilderHudView.paletteBitmap(terrainId: String): Bitmap? = (terrainButtons[terrainId]?.findViewByName(WorldBuilderHudView.TERRAIN_IMAGE) as? Image)?.bitmap?.base

fun WorldBuilderHudView.highlightedTerrainId(): String? {
    val border = selectionBorder ?: return null
    return terrainButtons.entries
        .firstOrNull { (_, button) ->
            val buttonPosition = button.parent!!.pos + button.pos
            buttonPosition.x == border.x + WorldBuilderHudView.SELECTION_BORDER && buttonPosition.y == border.y + WorldBuilderHudView.SELECTION_BORDER
        }?.key
}

suspend fun ViewsForTesting.clickPaletteTerrain(
    hudView: WorldBuilderHudView,
    terrainId: String,
) = clickOn(hudView.terrainButtons.getValue(terrainId))
