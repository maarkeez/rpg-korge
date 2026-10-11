package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton

// Page object of [BattlefieldView].

fun BattlefieldView.isBattlefieldDisplayed(): Boolean = isDisplayed

fun BattlefieldView.tileCount(): Int = if (isDisplayed) tiles.size else 0

fun BattlefieldView.hasTerrainAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.TERRAIN)

fun BattlefieldView.hasBattleUnitAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.BATTLE_UNIT)

/** Whether the battle unit on the tile is blinking in white, e.g.: when it receives an effect. */
fun BattlefieldView.isFlashingAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.FLASH)

/** The colour the battlefield is tinted with, or `null` when it is not tinted. */
fun BattlefieldView.tintColor(): RGBA? = tintOverlay?.color?.withA(255)

/** How opaque the battlefield tint is: 0.0 when it is not tinted. */
fun BattlefieldView.tintAlpha(): Double = tintOverlay?.alpha ?: 0.0

/** Whether the tile is darkened by the mask over the tiles that can not be selected. */
fun BattlefieldView.isDimmedAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.DIM)

/** Whether the battle unit on the tile is darkened by the mask, i.e.: the mask is displayed over it. */
fun BattlefieldView.isBattleUnitDimmedAt(
    row: Int,
    column: Int,
): Boolean {
    val layerNames = tile(row, column).children.map { child -> child.name }
    return BattlefieldView.BATTLE_UNIT in layerNames && layerNames.lastIndexOf(BattlefieldView.DIM) > layerNames.indexOf(BattlefieldView.BATTLE_UNIT)
}

fun BattlefieldView.dimmedTilesCount(): Int = if (!isDisplayed) 0 else tiles.count { tile -> tile.hasLayer(BattlefieldView.DIM) }

/** The (row, column) of every tile displaying a battle unit. */
fun BattlefieldView.battleUnitPositions(): List<Pair<Int, Int>> =
    if (!isDisplayed) {
        emptyList()
    } else {
        (0 until battlefieldGrid.rows)
            .flatMap { row -> (0 until battlefieldGrid.cols).map { column -> row to column } }
            .filter { (row, column) -> hasBattleUnitAt(row, column) }
    }

/** Whether the tile is highlighted (e.g.: movement range, cast range or selected tile). */
fun BattlefieldView.hasSelectionAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.SELECTION)

fun BattlefieldView.selectionCountAt(
    row: Int,
    column: Int,
): Int = tile(row, column).layerCount(BattlefieldView.SELECTION)

fun BattlefieldView.tilesWithSelectionCount(): Int = if (!isDisplayed) 0 else tiles.count { tile -> tile.hasLayer(BattlefieldView.SELECTION) }

/** How far the battlefield is scrolled inside the viewport, as (x, y). (0, 0) means not scrolled. */
fun BattlefieldView.scrollOffset(): Pair<Double, Double> = battlefieldGrid.x to battlefieldGrid.y

/** Whether the whole tile is inside the viewport, given how far the battlefield is scrolled. */
fun BattlefieldView.isTileVisible(
    row: Int,
    column: Int,
): Boolean {
    val tileX = column * BattlefieldView.TILE_SIZE + battlefieldGrid.x
    val tileY = row * BattlefieldView.TILE_SIZE + battlefieldGrid.y
    return tileX >= 0 &&
        tileY >= 0 &&
        tileX + BattlefieldView.TILE_SIZE <= BattlefieldView.VIEWPORT_WIDTH &&
        tileY + BattlefieldView.TILE_SIZE <= BattlefieldView.VIEWPORT_HEIGHT
}

suspend fun ViewsForTesting.clickTile(
    battlefieldView: BattlefieldView,
    row: Int,
    column: Int,
) = clickOn(battlefieldView.tile(row, column))

private fun UIButton.hasLayer(layerName: String): Boolean = layerCount(layerName) > 0

private fun UIButton.layerCount(layerName: String): Int = children.count { child -> child.name == layerName }
