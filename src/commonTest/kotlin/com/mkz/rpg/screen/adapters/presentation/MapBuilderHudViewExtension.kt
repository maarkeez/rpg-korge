package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [MapBuilderHudView].

fun MapBuilderHudView.rowsText(): String = rowsValue.text

fun MapBuilderHudView.columnsText(): String = columnsValue.text

/** The steps that can be applied to the rows, e.g.: `-8` is disabled with 5 rows. */
fun MapBuilderHudView.enabledRowsSteps(): List<Int> = rowsStepButtons.filterValues { button -> button.enabled }.keys.sorted()

fun MapBuilderHudView.enabledColumnsSteps(): List<Int> = columnsStepButtons.filterValues { button -> button.enabled }.keys.sorted()

suspend fun ViewsForTesting.clickRowsStep(
    hudView: MapBuilderHudView,
    step: Int,
) = clickOn(hudView.rowsStepButtons.getValue(step))

suspend fun ViewsForTesting.clickColumnsStep(
    hudView: MapBuilderHudView,
    step: Int,
) = clickOn(hudView.columnsStepButtons.getValue(step))

/** The terrains of the palette, in display order. */
fun MapBuilderHudView.paletteTerrainIds(): List<String> = terrainButtons.keys.toList()

/** The tile image of the terrain in the palette. */
fun MapBuilderHudView.paletteBitmap(terrainId: String): Bitmap? = (terrainButtons[terrainId]?.findViewByName(MapBuilderHudView.TERRAIN_IMAGE) as? Image)?.bitmap?.base

/** The terrain highlighted in the palette, or `null` when none is. */
fun MapBuilderHudView.highlightedTerrainId(): String? {
    val border = selectionBorder ?: return null
    return terrainButtons.entries
        .firstOrNull { (_, button) ->
            val buttonPosition = button.parent!!.pos + button.pos
            buttonPosition.x == border.x + MapBuilderHudView.SELECTION_BORDER && buttonPosition.y == border.y + MapBuilderHudView.SELECTION_BORDER
        }?.key
}

suspend fun ViewsForTesting.clickPaletteTerrain(
    hudView: MapBuilderHudView,
    terrainId: String,
) = clickOn(hudView.terrainButtons.getValue(terrainId))
