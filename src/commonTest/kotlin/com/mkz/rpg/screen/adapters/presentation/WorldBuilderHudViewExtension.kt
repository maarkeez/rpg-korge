package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [WorldBuilderHudView].

fun WorldBuilderHudView.rowsText(): String = rowsValue.text

fun WorldBuilderHudView.columnsText(): String = columnsValue.text

/** The steps that can be applied to the rows, e.g.: `-8` is disabled with 5 rows. */
fun WorldBuilderHudView.enabledRowsSteps(): List<Int> = rowsStepButtons.filterValues { button -> button.enabled }.keys.sorted()

fun WorldBuilderHudView.enabledColumnsSteps(): List<Int> = columnsStepButtons.filterValues { button -> button.enabled }.keys.sorted()

/** The steps of the rows displayed faded, as disabled ones. */
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

/** The terrains of the palette, in display order. */
fun WorldBuilderHudView.paletteTerrainIds(): List<String> = terrainButtons.keys.toList()

/** The tile image of the terrain in the palette. */
fun WorldBuilderHudView.paletteBitmap(terrainId: String): Bitmap? = (terrainButtons[terrainId]?.findViewByName(WorldBuilderHudView.TERRAIN_IMAGE) as? Image)?.bitmap?.base

/** The terrain highlighted in the palette, or `null` when none is. */
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
