package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
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

class BattlefieldView(
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : UIContainer(Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT)) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"

        const val TILE_PIXEL_SIZE = 16
        const val TILE_SIZE = TILE_PIXEL_SIZE * PIXEL_SCALE
        const val VISIBLE_TILES = 8
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        private const val TRANSITION_NAME_SEPARATOR = "_to_"
    }

    private var delegate: Delegate? = null
    private lateinit var terrainBitMaps: Map<String, Bitmap>
    private lateinit var transitionBitMaps: Map<String, List<Bitmap>>
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
        val builtTerrainBitMaps = mutableMapOf<String, Bitmap>()
        transitionBitMaps =
            buildMap {
                val transitionsRoot = resourcesVfs["terrain/transitions"]
                if (transitionsRoot.exists()) {
                    transitionsRoot
                        .listNames()
                        .filter { it.endsWith(".png") }
                        .forEach { fileName ->
                            val transitionName = fileName.removeSuffix(".png")
                            val transitionBitmap = transitionsRoot[fileName].readBitmap()
                            val transitionTiles =
                                List(transitionBitmap.width / TILE_PIXEL_SIZE) { index ->
                                    transitionBitmap.crop(index * TILE_PIXEL_SIZE, 0, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE)
                                }
                            val (fromTerrainId, toTerrainId) = transitionName.split(TRANSITION_NAME_SEPARATOR)
                            builtTerrainBitMaps[fromTerrainId] = transitionTiles.first()
                            builtTerrainBitMaps[toTerrainId] = transitionTiles.last()
                            put(transitionName, transitionTiles)
                        }
                }
            }
        terrainBitMaps = builtTerrainBitMaps
        sprites.load()
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
                    val tileDto = battlefield.tiles[PositionDto(row, column)]!!
                    tileButton.addImage(terrainTileBitmap(tileDto.terrainId, tileDto.terrainTransition), TERRAIN)
                    tileButton.onClick {
                        delegate?.tileSelected(row, column)
                    }
                }
            }
        }
    }

    fun terrainTileBitmap(
        terrainId: String,
        terrainTransition: TerrainTransitionDto?,
    ): Bitmap {
        val transition = terrainTransition
        return if (transition != null) {
            val transitionName = "${transition.fromTerrainId}$TRANSITION_NAME_SEPARATOR${transition.toTerrainId}"
            transitionBitMaps.getValue(transitionName)[transition.wangIndex]
        } else {
            terrainBitMaps[terrainId] ?: throw IllegalStateException("Terrain $terrainId bitmap not found")
        }
    }

    private fun tileName(
        row: Int,
        column: Int,
    ): String = "row-$row-column-$column"

    fun displayKnightBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, sprites.unit("knight"))
    }

    fun displayRatBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, sprites.unit("rat"))
    }

    fun displayBeeBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, sprites.unit("bee"))
    }

    private fun displayUnit(
        row: Int,
        column: Int,
        unitBitmap: Bitmap,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addImage(unitBitmap, BATTLE_UNIT)
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
        tileButton.addImage(sprites.highlight(SpriteRegistry.Highlight.MOVEMENT), SELECTION)
    }

    fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        if (tileButton.findViewByName(SELECTION) != null) return
        tileButton.addImage(sprites.highlight(SpriteRegistry.Highlight.CAST), SELECTION)
    }

    fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addImage(sprites.highlight(SpriteRegistry.Highlight.SELECTION), SELECTION)
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
            scale = PIXEL_SCALE.toDouble()
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
