package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.animate.Animator
import korlibs.korge.animate.alpha
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
import korlibs.korge.view.SolidRect
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.clipContainer
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.math.clamp
import korlibs.math.geom.Size
import korlibs.math.geom.Spacing
import korlibs.math.interpolation.EASE_OUT_QUAD
import korlibs.math.interpolation.Easing
import korlibs.time.milliseconds

open class BattlefieldView(
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIContainer(Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT)) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val LOOT = "LOOT"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"
        const val FLASH = "FLASH"
        const val DIM = "DIM"
        const val HEALTH_PREVIEW = "HEALTH_PREVIEW"

        const val TILE_SIZE = 48
        const val TILE_PIXEL_SIZE = 16
        const val VISIBLE_TILES = 8
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        // The loot is drawn smaller than the battle units, so the terrain shows around it.
        const val LOOT_SCALE = 2.0
        const val CHEST_PATH = "battlefield/chest.png"

        // How long a moving battle unit stops on each tile of its path.
        val MOVE_STEP_DURATION = 150.milliseconds
        val SCROLL_DURATION = 300.milliseconds

        // Opacity of the black mask over the tiles that can not be selected.
        const val DIM_ALPHA = 0.4

        // A battle unit receiving an effect blinks in white this many times.
        const val FLASH_COUNT = 3
        val FLASH_DURATION = 80.milliseconds

        // When a turn starts, the battlefield is tinted with the turn colour up to this opacity and back.
        const val TINT_ALPHA = 0.4
        val TINT_DURATION = 500.milliseconds

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
    internal lateinit var battlefieldAnimator: Animator

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
    internal var tintOverlay: SolidRect? = null

    init {
        addChild(viewport)
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    open fun displayBattlefield(battlefield: Battlefield.Dto) {
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
        // The drag offsets are relative to where the drag started, not to the previous drag event.
        var dragStartX = 0.0
        var dragStartY = 0.0
        viewport.onMouseDrag { event ->
            if (event.start) {
                dragStartX = battlefieldGrid.x
                dragStartY = battlefieldGrid.y
            }
            scrollTo(x = dragStartX + event.dx, y = dragStartY + event.dy)
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

    open fun terrainTileBitmap(
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

    open fun displayBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val unitBitmap = resourcesLoader.bitmapOrNull("unit/$unitId.png") ?: return
        val tileButton = tile(row, column)
        tileButton.addImage(unitBitmap, BATTLE_UNIT)
    }

    /**
     * Displays the items lying on the tile, below the battle unit standing on it: the item icon when there is one, or
     * a chest when there are several. Nothing is displayed when there are none.
     */
    open fun displayLoot(
        row: Int,
        column: Int,
        lootableItemIds: List<String>,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(LOOT)?.removeFromParent()
        val lootBitmap =
            when (lootableItemIds.size) {
                0 -> return
                1 -> resourcesLoader.bitmapOrNull("lootableItem/${lootableItemIds.single()}.png") ?: return
                else -> resourcesLoader.bitmap(CHEST_PATH)
            }
        val terrainIndex = tileButton.children.indexOfFirst { child -> child.name == TERRAIN }
        tileButton.addImage(lootBitmap, LOOT, scale = LOOT_SCALE)
        // Moved right above the terrain, so the battle units and the selection are drawn over it.
        val lootView = tileButton.findViewByName(LOOT)!!
        lootView.removeFromParent()
        tileButton.addChildAt(lootView, terrainIndex + 1)
    }

    interface Delegate {
        fun tileSelected(
            row: Int,
            column: Int,
        )
    }

    open fun displayPotentialMovement(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        tileButton.addImage(resourcesLoader.bitmap("battlefield/tile_selection_3.png"), SELECTION)
    }

    open fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        if (tileButton.findViewByName(SELECTION) != null) return
        tileButton.addImage(resourcesLoader.bitmap("battlefield/tile_selection_2.png"), SELECTION)
    }

    open fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addImage(resourcesLoader.bitmap("battlefield/tile_selection_4.png"), SELECTION)
    }

    /**
     * Displays the health the battle unit on the tile would have left, e.g.: after a cast, at the top of the tile.
     * Replaces the selection of the tile, so only the battle unit and its health are displayed.
     */
    open fun displayHealthPreview(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        removeHealthPreview(tileButton)
        val healthPreview = BattlefieldHealthBarPreviewView(Size(TILE_SIZE, TILE_SIZE / 5.0))
        healthPreview.name = HEALTH_PREVIEW
        healthPreview.display(previewOption)
        tileButton.addChild(healthPreview)
    }

    open fun resetTiles() {
        battlefieldGrid.children.forEach { view ->
            val tileButton = view as UIButton
            tileButton.findViewByName(SELECTION)?.removeFromParent()
            tileButton.findViewByName(DIM)?.removeFromParent()
            removeHealthPreview(tileButton)
        }
    }

    private fun removeHealthPreview(tileButton: UIButton) {
        val healthPreview = tileButton.findViewByName(HEALTH_PREVIEW) as? BattlefieldHealthBarPreviewView ?: return
        healthPreview.stop()
        healthPreview.removeFromParent()
    }

    /**
     * Darkens every tile but the given ones, including the battle units on them, so the tiles that can be selected
     * stand out. Removed when the tiles are reset.
     */
    open fun dimTilesExcept(tiles: Set<PositionDto>) {
        for (row in 0 until battlefieldGrid.rows) {
            for (column in 0 until battlefieldGrid.cols) {
                if (PositionDto(row, column) in tiles) continue
                val tileButton = tile(row, column)
                if (tileButton.findViewByName(DIM) != null) continue
                tileButton.solidRect(Size(TILE_SIZE, TILE_SIZE), Colors.BLACK) {
                    name = DIM
                    alpha = DIM_ALPHA
                }
            }
        }
    }

    open fun removeBattleUnit(
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
    open suspend fun moveBattleUnit(
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

    /** Tints the visible battlefield with the colour, fading it in and out, e.g.: when a turn starts. */
    open suspend fun tint(color: RGBA) {
        val overlay = viewport.solidRect(Size(VIEWPORT_WIDTH, VIEWPORT_HEIGHT), color) { alpha = 0.0 }
        tintOverlay = overlay
        val tintAnimator =
            overlay.animator {
                alpha(overlay, TINT_ALPHA, time = TINT_DURATION / 2)
                alpha(overlay, 0.0, time = TINT_DURATION / 2)
            }
        tintAnimator.awaitComplete()
        overlay.removeFromParent()
        tintOverlay = null
    }

    /**
     * Blinks the battle unit in white, after scrolling the battlefield so the battle unit is visible, e.g.: when it
     * receives an effect.
     */
    open suspend fun flashBattleUnit(
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
        scale: Double = 3.0,
    ) {
        val button = this
        val imageScale = scale
        button.image(bitmap) {
            name = viewName
            this.scale = imageScale
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
