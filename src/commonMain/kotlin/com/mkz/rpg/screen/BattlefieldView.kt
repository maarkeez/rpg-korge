package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TileDto
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animator
import korlibs.korge.animate.moveTo
import korlibs.korge.input.onClick
import korlibs.korge.input.onMouseDrag
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIGridFill
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiGridFill
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.clipContainer
import korlibs.korge.view.image
import korlibs.math.clamp
import korlibs.math.geom.Size
import korlibs.math.geom.Spacing
import korlibs.math.interpolation.EASE_OUT_QUAD
import korlibs.math.interpolation.Easing
import korlibs.time.milliseconds

class BattlefieldView : UIContainer(Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT)) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"

        const val TILE_SIZE = 48
        const val TILE_PIXEL_SIZE = 16
        const val VISIBLE_TILES = 8
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        private const val TRANSITION_NAME_SEPARATOR = "_to_"
        private const val WANG_INDEX_NO_TERRAIN_EDGES = 0
        private const val WANG_INDEX_ALL_TERRAIN_EDGES = 15
        private const val NORTH_TERRAIN_WEIGHT = 1
        private const val EAST_TERRAIN_WEIGHT = 2
        private const val SOUTH_TERRAIN_WEIGHT = 4
        private const val WEST_TERRAIN_WEIGHT = 8
    }

    private var delegate: Delegate? = null
    private lateinit var transitionBitMaps: Map<String, List<Bitmap>>
    private lateinit var knightBitmap: Bitmap
    private lateinit var ratBitmap: Bitmap
    private lateinit var beeBitmap: Bitmap
    private lateinit var tileSelection1BitMap: Bitmap
    private lateinit var tileSelection2BitMap: Bitmap
    private lateinit var tileSelection3BitMap: Bitmap
    private lateinit var tileSelectionBitMap: Bitmap
    private lateinit var battlefieldAnimator: Animator

    private val viewport =
        clipContainer(
            size = Size(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
        )
    private lateinit var battlefieldGrid: UIGridFill

    init {
        addChild(viewport)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    suspend fun loadAssets() {
        transitionBitMaps =
            buildMap {
                val transitionsRoot = resourcesVfs["terrain/transitions"]
                if (transitionsRoot.exists()) {
                    transitionsRoot
                        .listNames()
                        .filter { it.endsWith(".png") }
                        .forEach { fileName ->
                            val transitionBitmap = transitionsRoot[fileName].readBitmap()
                            put(
                                fileName.removeSuffix(".png"),
                                List(transitionBitmap.width / TILE_PIXEL_SIZE) { index ->
                                    transitionBitmap.crop(index * TILE_PIXEL_SIZE, 0, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE)
                                },
                            )
                        }
                }
            }
        knightBitmap = resourcesVfs["unit/knight.png"].readBitmap()
        ratBitmap = resourcesVfs["unit/rat.png"].readBitmap()
        beeBitmap = resourcesVfs["unit/bee.png"].readBitmap()
        tileSelection1BitMap = resourcesVfs["battlefield/tile_selection_1.png"].readBitmap()
        tileSelection2BitMap = resourcesVfs["battlefield/tile_selection_2.png"].readBitmap()
        tileSelection3BitMap = resourcesVfs["battlefield/tile_selection_3.png"].readBitmap()
        tileSelectionBitMap = resourcesVfs["battlefield/tile_selection_4.png"].readBitmap()
    }

    fun displayBattlefield(battlefield: Battlefield.Dto) {
        val mapHeight = TILE_SIZE * battlefield.rows
        val mapWidth = TILE_SIZE * battlefield.columns
        battlefieldGrid =
            viewport.uiGridFill(
                size = Size(width = mapWidth, height = mapHeight),
                spacing = Spacing(0.0, 0.0),
                cols = 0,
                rows = 0,
            )
        battlefieldAnimator = battlefieldGrid.animator(parallel = true)
        viewport.onMouseDrag { event ->
            val dx = event.dx
            val dy = event.dy

            val minX = -(mapWidth - VIEWPORT_WIDTH).toDouble()
            val minY = -(mapHeight - VIEWPORT_HEIGHT).toDouble()

            val newX = (battlefieldGrid.x + dx).clamp(minX, 0.0)
            val newY = (battlefieldGrid.y + dy).clamp(minY, 0.0)
            battlefieldAnimator
                .cancel()
                .moveTo(
                    view = battlefieldGrid,
                    x = { newX },
                    y = { newY },
                    time = 300.milliseconds,
                    easing = Easing.EASE_OUT_QUAD,
                )
        }
        battlefieldGrid.rows = battlefield.rows
        battlefieldGrid.cols = battlefield.columns
        for (row in 0 until battlefield.rows) {
            for (column in 0 until battlefield.columns) {
                battlefieldGrid.uiButton(label = "").also { tileButton ->
                    tileButton.bgColorOut = Colors.TRANSPARENT
                    tileButton.bgColorOver = Colors.TRANSPARENT
                    tileButton.background.borderColor = Colors.TRANSPARENT
                    tileButton.background.bgColor = Colors.TRANSPARENT
                    tileButton.name = tileName(row, column)
                    val terrainId = battlefield.tiles[PositionDto(row, column)]!!.terrainId
                    val wangIndex = terrainWangIndex(row, column, battlefield.tiles)
                    tileButton.addImage(terrainTileBitmap(terrainId, wangIndex), TERRAIN)
                    tileButton.onClick {
                        delegate?.tileSelected(row, column)
                    }
                }
            }
        }
    }

    fun terrainWangIndex(
        row: Int,
        column: Int,
        tiles: Map<PositionDto, TileDto>,
    ): Int {
        val terrainId = tiles.getValue(PositionDto(row, column)).terrainId
        var wangIndex = WANG_INDEX_NO_TERRAIN_EDGES
        if (hasDifferentAdjacentTerrain(tiles, terrainId, row - 1, column)) wangIndex += NORTH_TERRAIN_WEIGHT
        if (hasDifferentAdjacentTerrain(tiles, terrainId, row, column + 1)) wangIndex += EAST_TERRAIN_WEIGHT
        if (hasDifferentAdjacentTerrain(tiles, terrainId, row + 1, column)) wangIndex += SOUTH_TERRAIN_WEIGHT
        if (hasDifferentAdjacentTerrain(tiles, terrainId, row, column - 1)) wangIndex += WEST_TERRAIN_WEIGHT
        return wangIndex
    }

    fun terrainTileBitmap(
        terrainId: String,
        wangIndex: Int,
    ): Bitmap {
        val transition =
            transitionBitMaps.entries.firstOrNull { (transitionName, _) ->
                transitionName.split(TRANSITION_NAME_SEPARATOR).contains(terrainId)
            } ?: throw IllegalStateException("Terrain $terrainId bitmap not found")
        val (transitionName, transitionBitMap) = transition
        val bitmapIndex =
            if (transitionName.split(TRANSITION_NAME_SEPARATOR).first() == terrainId) {
                wangIndex
            } else {
                if (wangIndex == WANG_INDEX_ALL_TERRAIN_EDGES) WANG_INDEX_ALL_TERRAIN_EDGES else WANG_INDEX_ALL_TERRAIN_EDGES - wangIndex
            }
        return transitionBitMap[bitmapIndex]
    }

    private fun hasDifferentAdjacentTerrain(
        tiles: Map<PositionDto, TileDto>,
        terrainId: String,
        row: Int,
        column: Int,
    ): Boolean {
        val adjacentTile = tiles[PositionDto(row, column)] ?: return false
        return adjacentTile.terrainId != terrainId
    }

    private fun tileName(
        row: Int,
        column: Int,
    ): String = "row-$row-column-$column"

    fun displayKnightBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, knightBitmap)
    }

    fun displayRatBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, ratBitmap)
    }

    fun displayBeeBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, beeBitmap)
    }

    private fun displayUnit(
        row: Int,
        column: Int,
        beeBitmap: Bitmap,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addImage(beeBitmap, BATTLE_UNIT)
    }

    interface Delegate {
        fun tileSelected(
            row: Int,
            column: Int,
        )
    }

    fun displayPotentialMovement(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addImage(tileSelection3BitMap, SELECTION)
    }

    fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        if (tileButton.findViewByName(SELECTION) != null) return
        tileButton.addImage(tileSelection2BitMap, SELECTION)
    }

    fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addImage(tileSelectionBitMap, SELECTION)
    }

    fun resetTiles() {
        battlefieldGrid.children.forEach { view ->
            val tileButton = view as UIButton
            tileButton.findViewByName(SELECTION)?.removeFromParent()
        }
    }

    fun removeBattleUnit(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(BATTLE_UNIT)?.removeFromParent()
    }

    private fun UIButton.addImage(
        bitmap: Bitmap,
        viewName: String,
    ) {
        val button = this
        button.image(bitmap) {
            name = viewName
            scale = 3.0
            smoothing = false
            centerOn(button)
        }
    }

    private fun Bitmap.crop(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Bitmap {
        val croppedBitmap = Bitmap32(width, height, premultiplied = premultiplied)
        for (cropY in 0 until height) {
            for (cropX in 0 until width) {
                croppedBitmap.setRgba(cropX, cropY, getRgba(x + cropX, y + cropY))
            }
        }
        return croppedBitmap
    }
}
