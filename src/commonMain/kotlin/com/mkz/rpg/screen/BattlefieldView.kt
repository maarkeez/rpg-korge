package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.snapToArtPixel
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
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
import kotlin.math.floor
import kotlin.math.hypot

class BattlefieldView(
    private val sprites: SpriteRegistry = SpriteRegistry(),
    private val viewportSize: Size = Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT),
) : UIContainer(viewportSize) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"
        const val UNIT_OVERLAY = "UNIT_OVERLAY"
        const val INSPECT_GLYPH = "INSPECT_GLYPH"

        const val TILE_PIXEL_SIZE = 16
        const val TILE_SIZE = TILE_PIXEL_SIZE * PIXEL_SCALE
        const val VISIBLE_TILES = 8
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        /** Pointer travel (pt) beyond which a gesture is a drag and must not select a tile. */
        const val TAP_DRAG_THRESHOLD = 8.0

        private const val TRANSITION_NAME_SEPARATOR = "_to_"
    }

    private var delegate: Delegate? = null
    private lateinit var terrainBitMaps: Map<String, Bitmap>
    private lateinit var transitionBitMaps: Map<String, List<Bitmap>>
    private var mapWidth = 0.0
    private var mapHeight = 0.0
    private var scrollX = 0.0
    private var scrollY = 0.0
    private var gestureIsDrag = false

    private val viewport = clipContainer(size = viewportSize)
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
        mapHeight = (TILE_SIZE * battlefield.rows).toDouble()
        mapWidth = (TILE_SIZE * battlefield.columns).toDouble()
        battlefieldGrid =
            viewport.uiGridFill(
                size = Size(width = mapWidth, height = mapHeight),
                spacing = Spacing(0.0, 0.0),
                cols = 0,
                rows = 0,
            )
        scrollTo(0.0, 0.0)
        installDragScrolling()
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
                        if (!gestureIsDrag) delegate?.tileSelected(row, column)
                    }
                }
            }
        }
    }

    /** Scrolls so the tile is as close to the viewport center as the map bounds allow. */
    fun centerOn(
        row: Int,
        column: Int,
    ) {
        val tileCenterX = column * TILE_SIZE + TILE_SIZE / 2.0
        val tileCenterY = row * TILE_SIZE + TILE_SIZE / 2.0
        scrollTo(viewportSize.width / 2.0 - tileCenterX, viewportSize.height / 2.0 - tileCenterY)
    }

    private fun installDragScrolling() {
        var gestureStartScrollX = 0.0
        var gestureStartScrollY = 0.0
        viewport.onMouseDrag { drag ->
            if (drag.start) {
                gestureIsDrag = false
                gestureStartScrollX = scrollX
                gestureStartScrollY = scrollY
            }
            if (hypot(drag.dx, drag.dy) > TAP_DRAG_THRESHOLD) gestureIsDrag = true
            if (gestureIsDrag) scrollTo(gestureStartScrollX + drag.dx, gestureStartScrollY + drag.dy)
        }
    }

    /** Keeps the requested offset unsnapped so slow drags accumulate, and shows it snapped to whole art pixels. */
    private fun scrollTo(
        x: Double,
        y: Double,
    ) {
        scrollX = clampScroll(x, viewportSize.width, mapWidth)
        scrollY = clampScroll(y, viewportSize.height, mapHeight)
        battlefieldGrid.x = snapToArtPixel(scrollX)
        battlefieldGrid.y = snapToArtPixel(scrollY)
    }

    private fun clampScroll(
        offset: Double,
        viewportLength: Double,
        mapLength: Double,
    ): Double {
        if (mapLength <= viewportLength) return snapToArtPixel((viewportLength - mapLength) / 2.0)
        val minOffset = -floor((mapLength - viewportLength) / PIXEL_SCALE) * PIXEL_SCALE
        return offset.clamp(minOffset, 0.0)
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
        style: MovementStyle = MovementStyle.ALLY,
        hazard: Boolean = false,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addChild(MovementRangeTileView(style, hazard).also { it.name = SELECTION })
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
        tileButton.findViewByName(UNIT_OVERLAY)?.removeFromParent()
    }

    /** Draws or refreshes the HP bar and status pips of the unit on the tile. */
    fun displayUnitOverlay(
        row: Int,
        column: Int,
        state: UnitOverlayState,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        val overlay =
            tileButton.findViewByName(UNIT_OVERLAY) as? UnitOverlayView
                ?: UnitOverlayView().also { newOverlay ->
                    newOverlay.name = UNIT_OVERLAY
                    tileButton.addChild(newOverlay)
                }
        overlay.display(state)
    }

    /**
     * Marks the selected unit with corner brackets. Enemies use another color and also show an inspect glyph,
     * so the "inspecting an enemy" state doesn't rely on color.
     */
    fun displayUnitSelection(
        row: Int,
        column: Int,
        isEnemy: Boolean,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addChild(SelectionBracketsView(isEnemy).also { it.name = SELECTION })
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
