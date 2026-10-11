package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animator
import korlibs.korge.animate.moveTo
import korlibs.korge.input.onClick
import korlibs.korge.input.onMouseDrag
import korlibs.korge.time.delay
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
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIContainer(Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT)) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"
        const val FLASH = "FLASH"

        const val TILE_SIZE = 48
        const val TILE_PIXEL_SIZE = 16
        const val VISIBLE_TILES = 8
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        // How long a moving battle unit stops on each tile of its path.
        val MOVE_STEP_DURATION = 150.milliseconds
        val SCROLL_DURATION = 300.milliseconds

        // A battle unit receiving an effect blinks in white this many times.
        const val FLASH_COUNT = 3
        val FLASH_DURATION = 80.milliseconds

        private const val TRANSITION_NAME_SEPARATOR = "_to_"
    }

    private var delegate: Delegate? = null

    // Each transition strip is split into its terrain tiles, being the first and last ones the pure terrain tiles.
    private val transitionBitMaps: Map<String, List<Bitmap>> by lazy {
        resourcesLoader
            .bitmaps("terrain/transitions")
            .filterKeys { fileName -> fileName.endsWith(".png") }
            .map { (fileName, transitionBitmap) ->
                fileName.removeSuffix(".png") to
                    List(transitionBitmap.width / TILE_PIXEL_SIZE) { index ->
                        transitionBitmap.crop(index * TILE_PIXEL_SIZE, 0, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE)
                    }
            }.toMap()
    }
    private val terrainBitMaps: Map<String, Bitmap> by lazy {
        buildMap {
            transitionBitMaps.forEach { (transitionName, transitionTiles) ->
                val (fromTerrainId, toTerrainId) = transitionName.split(TRANSITION_NAME_SEPARATOR)
                put(fromTerrainId, transitionTiles.first())
                put(toTerrainId, transitionTiles.last())
            }
        }
    }
    private lateinit var battlefieldAnimator: Animator

    // White silhouette of each unit image, keyed by unit id.
    private val flashBitMaps = mutableMapOf<String, Bitmap>()

    // A battlefield smaller than the viewport can not be scrolled.
    private var minScrollX = 0.0
    private var minScrollY = 0.0

    private val viewport =
        clipContainer(
            size = Size(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
        )
    internal lateinit var battlefieldGrid: UIGridFill

    init {
        addChild(viewport)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
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
        minScrollX = minOf(0, VIEWPORT_WIDTH - mapWidth).toDouble()
        minScrollY = minOf(0, VIEWPORT_HEIGHT - mapHeight).toDouble()
        viewport.onMouseDrag { event ->
            scrollTo(x = battlefieldGrid.x + event.dx, y = battlefieldGrid.y + event.dy)
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

    internal val isDisplayed: Boolean
        get() = ::battlefieldGrid.isInitialized

    internal val tiles: List<UIButton>
        get() = battlefieldGrid.children.filterIsInstance<UIButton>()

    internal fun tile(
        row: Int,
        column: Int,
    ): UIButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton

    private fun tileName(
        row: Int,
        column: Int,
    ): String = "row-$row-column-$column"

    fun displayBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val unitBitmap = resourcesLoader.bitmapOrNull("unit/$unitId.png") ?: return
        val tileButton = tile(row, column)
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
        val tileButton = tile(row, column)
        tileButton.addImage(resourcesLoader.bitmap("battlefield/tile_selection_3.png"), SELECTION)
    }

    fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        if (tileButton.findViewByName(SELECTION) != null) return
        tileButton.addImage(resourcesLoader.bitmap("battlefield/tile_selection_2.png"), SELECTION)
    }

    fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addImage(resourcesLoader.bitmap("battlefield/tile_selection_4.png"), SELECTION)
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
        val tileButton = tile(row, column)
        tileButton.findViewByName(BATTLE_UNIT)?.removeFromParent()
    }

    /**
     * Moves the battle unit tile by tile along the path, stopping on each tile, and scrolls the battlefield so the
     * battle unit stays visible. Path: the tiles walked from the origin (excluded) to the destination (included).
     */
    suspend fun moveBattleUnit(
        fromRow: Int,
        fromColumn: Int,
        path: List<PositionDto>,
        unitId: String,
    ) {
        var previousPosition = PositionDto(fromRow, fromColumn)
        path.forEach { position ->
            followTile(position.row, position.column)
            removeBattleUnit(previousPosition.row, previousPosition.column)
            displayBattleUnit(position.row, position.column, unitId)
            delay(MOVE_STEP_DURATION)
            previousPosition = position
        }
    }

    /**
     * Blinks the battle unit in white, after scrolling the battlefield so the battle unit is visible, e.g.: when it
     * receives an effect.
     */
    suspend fun flashBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val unitBitmap = resourcesLoader.bitmapOrNull("unit/$unitId.png") ?: return
        val flashBitmap = flashBitMaps.getOrPut(unitId) { unitBitmap.whiteSilhouette() }
        followTile(row, column)
        val tileButton = tile(row, column)
        repeat(FLASH_COUNT) {
            tileButton.addImage(flashBitmap, FLASH)
            delay(FLASH_DURATION)
            tileButton.findViewByName(FLASH)?.removeFromParent()
            delay(FLASH_DURATION)
        }
    }

    // Centers the tile when it is out of the viewport or on its edge.
    private suspend fun followTile(
        row: Int,
        column: Int,
    ) {
        val tileX = column * TILE_SIZE.toDouble()
        val tileY = row * TILE_SIZE.toDouble()
        if (isAwayFromViewportEdges(tileX, battlefieldGrid.x, VIEWPORT_WIDTH) && isAwayFromViewportEdges(tileY, battlefieldGrid.y, VIEWPORT_HEIGHT)) return
        val centeredX = ((VIEWPORT_WIDTH - TILE_SIZE) / 2.0 - tileX).clamp(minScrollX, 0.0)
        val centeredY = ((VIEWPORT_HEIGHT - TILE_SIZE) / 2.0 - tileY).clamp(minScrollY, 0.0)
        if (centeredX == battlefieldGrid.x && centeredY == battlefieldGrid.y) return
        scrollTo(x = centeredX, y = centeredY)
        battlefieldAnimator.awaitComplete()
    }

    private fun isAwayFromViewportEdges(
        tileOffset: Double,
        scrollOffset: Double,
        viewportSize: Int,
    ): Boolean {
        val tileOffsetInViewport = tileOffset + scrollOffset
        return tileOffsetInViewport >= TILE_SIZE && tileOffsetInViewport + TILE_SIZE <= viewportSize - TILE_SIZE
    }

    private fun scrollTo(
        x: Double,
        y: Double,
    ) {
        val newX = x.clamp(minScrollX, 0.0)
        val newY = y.clamp(minScrollY, 0.0)
        battlefieldAnimator
            .cancel()
            .moveTo(
                view = battlefieldGrid,
                x = { newX },
                y = { newY },
                time = SCROLL_DURATION,
                easing = Easing.EASE_OUT_QUAD,
            )
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

    private fun Bitmap.whiteSilhouette(): Bitmap {
        val silhouette = Bitmap32(width, height, premultiplied = premultiplied)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (getRgba(x, y).a > 0) silhouette.setRgba(x, y, Colors.WHITE)
            }
        }
        return silhouette
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
