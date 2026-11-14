package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.View
import korlibs.math.geom.Point

fun BattlefieldView.isBattlefieldDisplayed(): Boolean = isDisplayed

fun BattlefieldView.tileCount(): Int = if (isDisplayed) rows * columns else 0

fun BattlefieldView.createdTileCount(): Int = if (isDisplayed) tiles.size else 0

fun BattlefieldView.isTileOnScreen(
    row: Int,
    column: Int,
): Boolean = tileOrNull(row, column)?.parent != null

fun BattlefieldView.tilesOnScreenCount(): Int = if (isDisplayed) battlefieldGrid.children.count { child -> child is UIButton } else 0

fun BattlefieldView.hasTerrainAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.TERRAIN)

fun BattlefieldView.hasShadowAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).background.shadowRadius > 0.0

fun BattlefieldView.hasBattleUnitAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tileOrNull(row, column)?.hasLayer(BattlefieldView.BATTLE_UNIT) == true

fun BattlefieldView.battleUnitBitmapAt(
    row: Int,
    column: Int,
): Bitmap? =
    tileOrNull(row, column)
        ?.findViewByName(BattlefieldView.BATTLE_UNIT)
        .layerImage()
        ?.bitmap
        ?.base

data class GlidingBattleUnit(
    val name: String,
    val x: Double,
    val y: Double,
)

fun BattlefieldView.glidingBattleUnits(): List<GlidingBattleUnit> =
    if (!isDisplayed) {
        emptyList()
    } else {
        movingBattleUnits.children.map { glidingUnit ->
            val sprite = (glidingUnit as Container).firstChild!!
            GlidingBattleUnit(
                name = glidingUnit.name!!,
                x = glidingUnit.x + sprite.x,
                y = glidingUnit.y + sprite.y,
            )
        }
    }

fun BattlefieldView.hasQuestMarkerAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tileOrNull(row, column)?.hasLayer(BattlefieldView.QUEST_MARKER) == true

fun BattlefieldView.questMarkerColorAt(
    row: Int,
    column: Int,
): RGBA? = (tileOrNull(row, column)?.findViewByName(BattlefieldView.QUEST_MARKER) as? QuestMarkerView)?.displayedColor()

fun BattlefieldView.hasNpcAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tileOrNull(row, column)?.hasLayer(BattlefieldView.NPC) == true

fun BattlefieldView.npcPlacementAt(
    row: Int,
    column: Int,
): TilePlacement? = imagePlacementAt(row, column, BattlefieldView.NPC)

fun BattlefieldView.battleUnitPlacementAt(
    row: Int,
    column: Int,
): TilePlacement? = imagePlacementAt(row, column, BattlefieldView.BATTLE_UNIT)

fun BattlefieldView.terrainPlacementAt(
    row: Int,
    column: Int,
): TilePlacement? = imagePlacementAt(row, column, BattlefieldView.TERRAIN)

private fun BattlefieldView.imagePlacementAt(
    row: Int,
    column: Int,
    layerName: String,
): TilePlacement? =
    tileOrNull(row, column)?.findViewByName(layerName).layerImage()?.let { image ->
        TilePlacement(image.x, image.y, image.scaledWidth, image.scaledHeight)
    }

fun BattlefieldView.hasBuildingAt(
    row: Int,
    column: Int,
): Boolean {
    val layerNames = tileOrNull(row, column)?.children?.map { child -> child.name } ?: return false
    return isDisplayed && BattlefieldView.BUILDING in layerNames && layerNames.indexOf(BattlefieldView.BUILDING) == layerNames.indexOf(BattlefieldView.TERRAIN) + 1
}

fun BattlefieldView.isBlinkingAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tileOrNull(row, column)?.hasLayer(BattlefieldView.BLINK) == true

fun BattlefieldView.tintColor(): RGBA? = tintOverlay?.color?.withA(255)

fun BattlefieldView.tintAlpha(): Double = tintOverlay?.alpha ?: 0.0

fun BattlefieldView.isDimmedAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tile(row, column).hasLayer(BattlefieldView.DIM)

fun BattlefieldView.isBattleUnitDimmedAt(
    row: Int,
    column: Int,
): Boolean {
    val layerNames = tile(row, column).children.map { child -> child.name }
    return BattlefieldView.BATTLE_UNIT in layerNames && layerNames.lastIndexOf(BattlefieldView.DIM) > layerNames.indexOf(BattlefieldView.BATTLE_UNIT)
}

fun BattlefieldView.dimmedTilesCount(): Int = if (!isDisplayed) 0 else tiles.count { tile -> tile.hasLayer(BattlefieldView.DIM) }

fun BattlefieldView.battleUnitPositions(): List<Pair<Int, Int>> =
    if (!isDisplayed) {
        emptyList()
    } else {
        (0 until rows)
            .flatMap { row -> (0 until columns).map { column -> row to column } }
            .filter { (row, column) -> hasBattleUnitAt(row, column) }
    }

fun BattlefieldView.hasSelectionAt(
    row: Int,
    column: Int,
): Boolean = isDisplayed && tileOrNull(row, column)?.hasLayer(BattlefieldView.SELECTION) == true

fun BattlefieldView.selectionCountAt(
    row: Int,
    column: Int,
): Int = tile(row, column).layerCount(BattlefieldView.SELECTION)

fun BattlefieldView.tilesWithSelectionCount(): Int = if (!isDisplayed) 0 else tiles.count { tile -> tile.hasLayer(BattlefieldView.SELECTION) }

fun BattlefieldView.scrollOffset(): Pair<Double, Double> = battlefieldGrid.x to battlefieldGrid.y

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

fun BattlefieldView.terrainBitmapAt(
    row: Int,
    column: Int,
): Bitmap? =
    tile(row, column)
        .findViewByName(BattlefieldView.TERRAIN)
        .layerImage()
        ?.bitmap
        ?.base

fun BattlefieldView.displayedBattlefieldCount(): Int = if (!isDisplayed) 0 else battlefieldGrid.parent!!.children.count { child -> child.name == BattlefieldView.BATTLEFIELD }

fun BattlefieldView.lootBitmapAt(
    row: Int,
    column: Int,
): Bitmap? = (tile(row, column).findViewByName(BattlefieldView.DROPPED_ITEMS) as? Image)?.bitmap?.base

fun BattlefieldView.isLootRightAboveTerrainAt(
    row: Int,
    column: Int,
): Boolean {
    val layerNames = tile(row, column).children.map { child -> child.name }
    return layerNames.indexOf(BattlefieldView.DROPPED_ITEMS) == layerNames.indexOf(BattlefieldView.TERRAIN) + 1
}

data class LiftedLoot(
    val bitmap: Bitmap?,
    val rise: Double,
    val scale: Double,
    val alpha: Double,
)

fun BattlefieldView.liftedLootAt(
    row: Int,
    column: Int,
): LiftedLoot? {
    val liftedView = tile(row, column).findViewByName(BattlefieldView.LOOTED_ITEM) as? Image ?: return null
    return LiftedLoot(
        bitmap = liftedView.bitmap.base,
        rise = BattlefieldView.TILE_SIZE / 2.0 - liftedView.y,
        scale = liftedView.scaleX,
        alpha = liftedView.alpha,
    )
}

fun BattlefieldView.isLiftedLootOverBattleUnitAt(
    row: Int,
    column: Int,
): Boolean {
    val layerNames = tile(row, column).children.map { child -> child.name }
    return layerNames.indexOf(BattlefieldView.LOOTED_ITEM) > layerNames.indexOf(BattlefieldView.BATTLE_UNIT)
}

suspend fun ViewsForTesting.dragBattlefield(
    battlefieldView: BattlefieldView,
    vararg offsets: Pair<Double, Double>,
) {
    val start = battlefieldView.localToGlobal(Point(BattlefieldView.VIEWPORT_WIDTH / 2.0, BattlefieldView.VIEWPORT_HEIGHT / 2.0))
    mouseMoveTo(start.x, start.y)
    mouseDown()
    offsets.forEach { (dx, dy) ->
        mouseMoveTo(start.x + dx, start.y + dy)
        battlefieldView.battlefieldAnimator.awaitComplete()
    }
    mouseUp()
}

suspend fun ViewsForTesting.clickTile(
    battlefieldView: BattlefieldView,
    row: Int,
    column: Int,
) = clickOn(battlefieldView.tile(row, column))

private fun View?.layerImage(): Image? =
    when (this) {
        is Image -> this
        is TerrainTileView -> terrainImage
        is UnitSpriteView -> spriteImage
        is TileHighlightView -> frameImage
        else -> null
    }

private fun UIButton.hasLayer(layerName: String): Boolean = layerCount(layerName) > 0

private fun UIButton.layerCount(layerName: String): Int = children.count { child -> child.name == layerName }

data class TilePlacement(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

private fun BattlefieldView.healthPreview(
    row: Int,
    column: Int,
): BattlefieldHealthBarPreviewView? = tile(row, column).findViewByName(BattlefieldView.HEALTH_PREVIEW) as? BattlefieldHealthBarPreviewView

fun BattlefieldView.healthPreviewAt(
    row: Int,
    column: Int,
): BattlefieldHealthBarPreviewView.PreviewOption? = healthPreview(row, column)?.previewOption

fun BattlefieldView.healthPreviewPlacementAt(
    row: Int,
    column: Int,
): TilePlacement? = healthPreview(row, column)?.let { preview -> TilePlacement(preview.x, preview.y, preview.width, preview.height) }

fun BattlefieldView.blinkedHealthAt(
    row: Int,
    column: Int,
): BattlefieldHealthBarPreviewView.PreviewOption? = (tile(row, column).findViewByName(BattlefieldView.HEALTH_BLINK) as? BattlefieldHealthBarPreviewView)?.previewOption

fun BattlefieldView.blinkedHealthBarAt(
    row: Int,
    column: Int,
): BattlefieldHealthBarPreviewView? = tile(row, column).findViewByName(BattlefieldView.HEALTH_BLINK) as? BattlefieldHealthBarPreviewView

fun BattlefieldView.blinkedHealthCountAt(
    row: Int,
    column: Int,
): Int = tile(row, column).children.count { child -> child.name == BattlefieldView.HEALTH_BLINK }

fun BattlefieldView.glidingBlinkedHealth(): List<BattlefieldHealthBarPreviewView.PreviewOption?> =
    if (!isDisplayed) {
        emptyList()
    } else {
        movingBattleUnits.children.map { glidingUnit ->
            ((glidingUnit as Container).findViewByName(BattlefieldView.HEALTH_BLINK) as? BattlefieldHealthBarPreviewView)?.previewOption
        }
    }

fun BattlefieldView.blinkedHealthBlinksAt(
    row: Int,
    column: Int,
) = (tile(row, column).findViewByName(BattlefieldView.HEALTH_BLINK) as? BattlefieldHealthBarPreviewView)?.blinks().orEmpty()

fun BattlefieldView.healthPreviewBlinksAt(
    row: Int,
    column: Int,
) = healthPreview(row, column)?.blinks().orEmpty()
