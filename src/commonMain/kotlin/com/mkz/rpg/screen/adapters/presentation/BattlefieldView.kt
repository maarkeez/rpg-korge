package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.io.async.launchImmediately
import korlibs.korge.animate.Animator
import korlibs.korge.animate.alpha
import korlibs.korge.animate.animator
import korlibs.korge.animate.moveBy
import korlibs.korge.animate.moveTo
import korlibs.korge.animate.scaleTo
import korlibs.korge.input.onClick
import korlibs.korge.input.onMouseDrag
import korlibs.korge.time.delay
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.anchor
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.math.clamp
import korlibs.math.geom.Anchor
import korlibs.math.geom.Size
import korlibs.math.interpolation.EASE_IN_QUAD
import korlibs.math.interpolation.EASE_OUT_QUAD
import korlibs.math.interpolation.Easing
import korlibs.time.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sign
import kotlin.time.Duration
import kotlin.time.DurationUnit

open class BattlefieldView(
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIContainer(Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT)) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val LOOT = "LOOT"
        const val PICKED_UP_LOOT = "PICKED_UP_LOOT"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val NPC = "NPC"
        const val BUILDING = "BUILDING"
        const val SELECTION = "SELECTION"
        const val FLASH = "FLASH"
        const val DIM = "DIM"
        const val HEALTH_PREVIEW = "HEALTH_PREVIEW"
        const val HEALTH_FLASH = "HEALTH_FLASH"
        const val BATTLEFIELD = "BATTLEFIELD"
        const val MOVING_BATTLE_UNITS = "MOVING_BATTLE_UNITS"
        const val GLIDING_BATTLE_UNIT = "GLIDING_BATTLE_UNIT"
        const val SWAPPING_LEADER = "SWAPPING_LEADER"
        const val SWAPPING_MEMBER = "SWAPPING_MEMBER"

        // The tiles are only created in the viewport and this many tiles around it, so a battlefield of any size
        // displays as fast as a small one.
        const val TILE_WINDOW_MARGIN = 1

        // A tile holding one of these layers keeps them while it is out of the window. The other tiles are created again
        // from the battlefield when they come back into it.
        private val KEPT_LAYERS = setOf(BUILDING, LOOT, BATTLE_UNIT, NPC, SELECTION, HEALTH_PREVIEW, FLASH, HEALTH_FLASH, PICKED_UP_LOOT)

        // A tile animating one of these layers stays on the screen until it is done, as its animation only plays there.
        private val ANIMATED_LAYERS = setOf(FLASH, HEALTH_FLASH, PICKED_UP_LOOT)

        // A tile is 64px, so all its art is drawn at a whole scale: its 16x16 art (the terrain, most units and npcs) at 4x,
        // and its 32x32 art at 2x.
        const val TILE_SIZE = 64
        const val TILE_PIXEL_SIZE = 16
        const val VISIBLE_TILES = 6
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        // The loot is drawn smaller than the battle units, so the terrain shows around it.
        const val LOOT_SCALE = 2.0
        const val CHEST_PATH = "battlefield/chest.png"

        // A picked up item rises this far from the center of its tile, growing to this scale while it fades out.
        const val PICK_UP_RISE = TILE_SIZE / 2.0
        const val PICK_UP_SCALE = 3.0
        val PICK_UP_DURATION = 400.milliseconds

        // How long a moving battle unit glides from one tile of its path to the next.
        val MOVE_STEP_DURATION = 150.milliseconds

        // Exploring, the party glides one tile at a time, all its battle units at once.
        val EXPLORING_STEP_DURATION = 250.milliseconds

        // Exploring, the CPU battle units wander quicker than the party walks.
        val WANDERING_STEP_DURATION = 75.milliseconds
        val SCROLL_DURATION = 300.milliseconds

        // Trailing, the leader moves freely between these offsets of the viewport (its 2x2 tiles at the center), and pushes
        // the camera along once it walks past them.
        const val DEADZONE_START = TILE_SIZE * (VISIBLE_TILES / 2 - 1)
        const val DEADZONE_END = TILE_SIZE * (VISIBLE_TILES / 2)

        // Trailing, a camera far from the leader catches up, closing 1 - e^(-dt / CATCH_UP_TIME) of the distance each frame,
        // and at least as fast as this, in pixels per second (twice the party walking), so it reaches a walking leader.
        val CATCH_UP_TIME = 80.milliseconds
        const val CATCH_UP_MIN_SPEED = TILE_SIZE * 8.0

        // Swapping, the leader hops over the party member this high, half-way, while the member slides underneath. A whole
        // number of pixels, so the hop lands where it started.
        val SWAP_DURATION = 150.milliseconds
        const val SWAP_HOP_HEIGHT = (TILE_SIZE / 3).toDouble()

        // Opacity of the black mask over the tiles that can not be selected.
        const val DIM_ALPHA = 0.4

        // A battle unit receiving an effect blinks in white this many times.
        const val FLASH_COUNT = 3
        val FLASH_DURATION = 80.milliseconds

        // While exploring, the health of a battle unit whose health changes stays this long over it, blinking as long as
        // it flashes, and fading out at the end.
        val HEALTH_FLASH_DURATION = 1500.milliseconds
        val HEALTH_FLASH_FADE_DURATION = 200.milliseconds

        // When a turn starts, the battlefield is tinted with the turn colour up to this opacity and back.
        const val TINT_ALPHA = 0.4
        val TINT_DURATION = 500.milliseconds

        /** The scale at which the bitmap fills a tile, whatever the size of its art (16x16 or 32x32). */
        internal fun tileFillingScale(bitmap: Bitmap): Double = TILE_SIZE.toDouble() / bitmap.width
    }

    private var delegate: Delegate? = null

    // The art of the terrain tiles, shared by all the tiles.
    private val terrainBitmaps = TerrainBitmaps(resourcesLoader)
    internal lateinit var battlefieldAnimator: Animator

    // The flashes blinking or waiting on each tile, stopped once its battle unit leaves it.
    private val flashes = mutableMapOf<TilePosition, MutableList<Job>>()

    // The health flashed while exploring, wherever it is (e.g.: gliding with its battle unit), until it is removed.
    private val healthFlashes = mutableSetOf<BattlefieldHealthBarPreviewView>()

    // The picked up items rising from their tile, drawn at whole pixels like the other views moving on the battlefield.
    private val risingLoot = mutableSetOf<View>()

    // A battlefield smaller than the viewport can not be scrolled.
    private var minScrollX = 0.0
    private var minScrollY = 0.0

    private val viewport =
        clipContainer(
            size = Size(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
        )

    // Holds the tiles at their position in the battlefield, and scrolls them inside the viewport.
    internal lateinit var battlefieldGrid: Container

    // Holds the battle units while they glide between tiles (e.g.: swapping), over the tiles and at their scroll, so they
    // are neither sorted with the tiles nor drawn under the tiles below them.
    internal lateinit var movingBattleUnits: Container

    // Updated on every frame after the tiles and the gliding battle units, so the scroll follows where they are drawn.
    private lateinit var camera: View
    internal var tintOverlay: SolidRect? = null

    // While exploring, the camera trails the leader: its tile, or the image it glides with meanwhile. `null` when the
    // camera does not trail.
    private var trailedTile: TilePosition? = null
    private var trailedView: View? = null

    // The trailing is paused by a drag, until the leader moves again.
    private var isTrailingPaused = false

    // The camera eases towards the leader when it is far from it, e.g.: when the exploration resumes.
    private var isCatchingUp = false

    private val isTrailing: Boolean
        get() = trailedTile != null

    // The displayed battlefield, which the tiles are created from.
    private lateinit var battlefield: BattlefieldTerrain
    internal val rows: Int get() = battlefield.rows
    internal val columns: Int get() = battlefield.columns

    // The tiles created so far, on the screen or kept out of the window.
    private val createdTiles = mutableMapOf<TilePosition, UIButton>()

    // The tiles left undimmed while the others are dimmed, or `null` when no tile is dimmed.
    private var undimmedTiles: Set<TilePosition>? = null

    // The scroll the tiles on the screen were chosen for.
    private var windowScroll: Pair<Double, Double>? = null

    init {
        addChild(viewport)
        // The drag offsets are relative to where the drag started, not to the previous drag event.
        var dragStartX = 0.0
        var dragStartY = 0.0
        viewport.onMouseDrag { event ->
            if (!isDisplayed) return@onMouseDrag
            if (event.start) {
                isTrailingPaused = isTrailing
                dragStartX = battlefieldGrid.x
                dragStartY = battlefieldGrid.y
            }
            // In the units of the grid, so it follows the finger also when a parent is scaled, e.g.: the Storybook zoomed.
            val drag = event.localDXY(battlefieldGrid)
            scrollTo(x = dragStartX + drag.x, y = dragStartY + drag.y)
        }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /**
     * Displays the battlefield terrain. When a battlefield is already displayed (e.g.: a map resized while it is designed),
     * it is replaced, keeping how far it was scrolled as far as the new size allows.
     */
    open fun displayBattlefield(battlefield: BattlefieldTerrain) {
        val mapHeight = TILE_SIZE * battlefield.rows
        val mapWidth = TILE_SIZE * battlefield.columns
        val previousScroll = if (isDisplayed) battlefieldGrid.x to battlefieldGrid.y else 0.0 to 0.0
        if (isDisplayed) {
            battlefieldAnimator.cancel()
            createdTiles.values.forEach(::removeHealthPreview)
            removeHealthFlashes()
            battlefieldGrid.removeFromParent()
            movingBattleUnits.removeFromParent()
            camera.removeFromParent()
        }
        createdTiles.clear()
        undimmedTiles = null
        windowScroll = null
        trailedView = null
        this.battlefield = battlefield
        minScrollX = minOf(0, VIEWPORT_WIDTH - mapWidth).toDouble()
        minScrollY = minOf(0, VIEWPORT_HEIGHT - mapHeight).toDouble()
        battlefieldGrid =
            viewport.container {
                name = BATTLEFIELD
                x = previousScroll.first.clamp(minScrollX, 0.0)
                y = previousScroll.second.clamp(minScrollY, 0.0)
            }
        battlefieldAnimator = battlefieldGrid.animator(parallel = true)
        movingBattleUnits = viewport.container { name = MOVING_BATTLE_UNITS }
        // The gliding battle units and the tiles on the screen follow the scroll, e.g.: while it is trailed, dragged or animated.
        camera =
            viewport.container {
                addUpdater { dt ->
                    trailLeader(dt)
                    drawAtWholePixels()
                    movingBattleUnits.pos = battlefieldGrid.pos
                    refreshTileWindow()
                }
            }
    }

    // The rows and the columns of the tiles in the viewport and around it.
    private fun tileWindow(): Pair<IntRange, IntRange> {
        val firstRow = (-battlefieldGrid.y / TILE_SIZE).toInt() - TILE_WINDOW_MARGIN
        val lastRow = ((-battlefieldGrid.y + VIEWPORT_HEIGHT - 1) / TILE_SIZE).toInt() + TILE_WINDOW_MARGIN
        val firstColumn = (-battlefieldGrid.x / TILE_SIZE).toInt() - TILE_WINDOW_MARGIN
        val lastColumn = ((-battlefieldGrid.x + VIEWPORT_WIDTH - 1) / TILE_SIZE).toInt() + TILE_WINDOW_MARGIN
        return firstRow.coerceAtLeast(0)..lastRow.coerceAtMost(rows - 1) to firstColumn.coerceAtLeast(0)..lastColumn.coerceAtMost(columns - 1)
    }

    private fun isInTileWindow(position: TilePosition): Boolean {
        val (windowRows, windowColumns) = tileWindow()
        return position.row in windowRows && position.column in windowColumns
    }

    /**
     * Puts on the screen the tiles in the window, creating them when needed, and takes the others off it. The tiles off
     * the screen are only kept when they hold more than their terrain (e.g.: a battle unit).
     */
    private fun refreshTileWindow() {
        val scroll = battlefieldGrid.x to battlefieldGrid.y
        if (scroll == windowScroll) return
        windowScroll = scroll
        // Copied as pairs, as the entries of a map can not be read once it changes (e.g.: on JS).
        createdTiles.toList().forEach { (position, tileButton) ->
            if (isInTileWindow(position) || tileButton.hasLayerIn(ANIMATED_LAYERS)) return@forEach
            tileButton.removeFromParent()
            if (!tileButton.hasLayerIn(KEPT_LAYERS)) createdTiles.remove(position)
        }
        val (windowRows, windowColumns) = tileWindow()
        for (row in windowRows) {
            for (column in windowColumns) {
                val tileButton = tile(row, column)
                if (tileButton.parent == null) battlefieldGrid.addChild(tileButton)
            }
        }
        // Each row is drawn over the previous one, e.g.: so a picked up item rises over the tile above.
        battlefieldGrid.sortChildrenBy(compareBy<View>({ view -> view.y }, { view -> view.x }))
    }

    private fun UIButton.hasLayerIn(layers: Set<String>): Boolean = children.any { child -> child.name in layers }

    private fun createTile(position: TilePosition): UIButton {
        val tileButton = UIButton(size = Size(TILE_SIZE, TILE_SIZE), text = "")
        tileButton.bgColorOut = Colors.TRANSPARENT
        tileButton.bgColorOver = Colors.TRANSPARENT
        tileButton.background.borderColor = Colors.TRANSPARENT
        tileButton.background.bgColor = Colors.TRANSPARENT
        // Without a shadow, as each tile's shadow would draw a line over the borders of its neighbours, like a grid.
        tileButton.elevation = false
        tileButton.name = tileName(position.row, position.column)
        tileButton.x = position.column * TILE_SIZE.toDouble()
        tileButton.y = position.row * TILE_SIZE.toDouble()
        val terrainTile = battlefield.tiles.getValue(position)
        tileButton.addChild(
            TerrainTileView(terrainBitmaps).apply {
                name = TERRAIN
                display(terrainTile.terrainId, terrainTile.transition)
            },
        )
        if (undimmedTiles?.contains(position) == false) tileButton.dim()
        tileButton.onClick {
            delegate?.tileSelected(position.row, position.column)
        }
        return tileButton
    }

    /**
     * Draws again the terrain of the tiles, e.g.: a painted tile and its neighbours, whose transitions may have changed.
     * The tiles not created yet will be created with it.
     */
    open fun updateTerrain(
        battlefield: BattlefieldTerrain,
        positions: Collection<TilePosition>,
    ) {
        this.battlefield = battlefield
        positions.forEach { position ->
            val terrainTile = battlefield.tiles[position] ?: return@forEach
            val terrainTileView = createdTiles[position]?.findViewByName(TERRAIN) as? TerrainTileView ?: return@forEach
            terrainTileView.display(terrainTile.terrainId, terrainTile.transition)
        }
    }

    /** The tile of the terrain, or of its transition to another terrain: what its [TerrainTileView] draws. */
    open fun terrainTileBitmap(
        terrainId: String,
        terrainTransition: TerrainTransition?,
    ): Bitmap = terrainBitmaps.bitmap(terrainId, terrainTransition)

    internal val isDisplayed: Boolean
        get() = ::battlefieldGrid.isInitialized

    /** The tiles created so far: the ones on the screen, and the ones kept out of the window. */
    internal val tiles: List<UIButton>
        get() = createdTiles.values.toList()

    /** The tile, created when it was not. It is on the screen when it is in the window. */
    internal fun tile(
        row: Int,
        column: Int,
    ): UIButton {
        val position = TilePosition(row, column)
        createdTiles[position]?.let { tileButton -> return tileButton }
        val tileButton = createTile(position)
        createdTiles[position] = tileButton
        if (isInTileWindow(position)) {
            battlefieldGrid.addChild(tileButton)
            battlefieldGrid.sortChildrenBy(compareBy<View>({ view -> view.y }, { view -> view.x }))
        }
        return tileButton
    }

    /** The tile, or `null` when it has not been created, so it only displays its terrain. */
    internal fun tileOrNull(
        row: Int,
        column: Int,
    ): UIButton? = createdTiles[TilePosition(row, column)]

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
        tileButton.addChild(UnitSpriteView(unitBitmap).apply { name = BATTLE_UNIT })
    }

    /** Displays the npc standing on the tile, as a battle unit. Unit: a recruitable npc is displayed as its unit. */
    open fun displayNpc(
        row: Int,
        column: Int,
        npcId: String,
        unitId: String?,
    ) {
        val npcBitmap = resourcesLoader.bitmapOrNull(unitId?.let { "unit/$it.png" } ?: "npc/$npcId.png") ?: return
        tile(row, column).addChild(UnitSpriteView(npcBitmap).apply { name = NPC })
    }

    /**
     * Displays the building standing on the tiles from the top-left one, as many as its image spans: each tile draws its
     * piece of the image right above its terrain, so the selection is drawn over it. Nothing is displayed when the
     * building has no image.
     */
    open fun displayBuilding(
        row: Int,
        column: Int,
        buildingId: String,
    ) {
        val buildingBitmap = resourcesLoader.bitmapOrNull("building/$buildingId.png") ?: return
        for (buildingRow in 0 until buildingBitmap.height / TILE_PIXEL_SIZE) {
            for (buildingColumn in 0 until buildingBitmap.width / TILE_PIXEL_SIZE) {
                val tileButton = tile(row + buildingRow, column + buildingColumn)
                val terrainIndex = tileButton.children.indexOfFirst { child -> child.name == TERRAIN }
                val pieceBitmap =
                    buildingBitmap.crop(buildingColumn * TILE_PIXEL_SIZE, buildingRow * TILE_PIXEL_SIZE, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE)
                tileButton.addImage(pieceBitmap, BUILDING)
                val pieceView = tileButton.findViewByName(BUILDING)!!
                pieceView.removeFromParent()
                tileButton.addChildAt(pieceView, terrainIndex + 1)
            }
        }
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

    /**
     * Lifts the picked up item off its tile, after scrolling the battlefield so the tile is visible: its icon rises over
     * the battle unit, growing and fading out, while the tile displays the items left on it. Nothing is lifted when the
     * item has no image.
     */
    open suspend fun pickUpLoot(
        row: Int,
        column: Int,
        lootableItemId: String,
        remainingLootableItemIds: List<String>,
    ) {
        displayLoot(row = row, column = column, lootableItemIds = remainingLootableItemIds)
        val itemBitmap = resourcesLoader.bitmapOrNull("lootableItem/$lootableItemId.png") ?: return
        followTile(row, column)
        // Anchored on its center, so it grows around it.
        val pickedUpView =
            tile(row, column).image(itemBitmap) {
                name = PICKED_UP_LOOT
                scale = LOOT_SCALE
                smoothing = false
                anchor(Anchor.CENTER)
                x = TILE_SIZE / 2.0
                y = TILE_SIZE / 2.0
            }
        risingLoot += pickedUpView
        try {
            pickedUpView
                .animator(parallel = true) {
                    moveBy(pickedUpView, x = 0.0, y = -PICK_UP_RISE, time = PICK_UP_DURATION, easing = Easing.EASE_OUT_QUAD)
                    scaleTo(pickedUpView, PICK_UP_SCALE, time = PICK_UP_DURATION)
                    alpha(pickedUpView, 0.0, time = PICK_UP_DURATION, easing = Easing.EASE_IN_QUAD)
                }.awaitComplete()
        } finally {
            risingLoot -= pickedUpView
        }
        pickedUpView.removeFromParent()
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
        tileButton.addHighlight(TileHighlightView.Kind.MOVE_RANGE)
    }

    open fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        if (tileButton.findViewByName(SELECTION) != null) return
        tileButton.addHighlight(TileHighlightView.Kind.CAST_RANGE)
    }

    open fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addHighlight(TileHighlightView.Kind.SELECTED)
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
        undimmedTiles = null
        createdTiles.values.forEach { tileButton ->
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
     * Displays the health the battle unit on the tile has left at the top of the tile, e.g.: when standing on lava while
     * exploring, for [HEALTH_FLASH_DURATION]: blinking as long as a flash, then still, and fading out at the end. It
     * keeps the selection of the tile, replaces the health already flashed on it, and follows its battle unit when it
     * moves, until it is removed from its tile (e.g.: defeated).
     */
    open suspend fun flashHealth(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        val tileButton = tile(row, column)
        tileButton.removeHealthFlash()
        val healthBar = BattlefieldHealthBarPreviewView(Size(TILE_SIZE, TILE_SIZE / 5.0))
        healthBar.name = HEALTH_FLASH
        healthBar.display(previewOption)
        tileButton.addChild(healthBar)
        healthFlashes += healthBar
        val blinkDuration = FLASH_DURATION * FLASH_COUNT * 2
        delay(blinkDuration)
        // Unless it was replaced or removed meanwhile, wherever it is.
        if (healthBar !in healthFlashes) return
        healthBar.hold()
        delay(HEALTH_FLASH_DURATION - blinkDuration - HEALTH_FLASH_FADE_DURATION)
        if (healthBar !in healthFlashes) return
        // Animated by the battlefield, which stays on the screen, so the fade ends also when the health is removed meanwhile.
        animator { alpha(healthBar, 0.0, time = HEALTH_FLASH_FADE_DURATION) }.awaitComplete()
        if (healthBar in healthFlashes) removeHealthFlash(healthBar)
    }

    /** Removes the health flashed on every tile, e.g.: when a battle starts. */
    open fun removeHealthFlashes() {
        healthFlashes.toList().forEach(::removeHealthFlash)
    }

    private fun UIButton.removeHealthFlash() {
        val healthBar = findViewByName(HEALTH_FLASH) as? BattlefieldHealthBarPreviewView ?: return
        removeHealthFlash(healthBar)
    }

    private fun removeHealthFlash(healthBar: BattlefieldHealthBarPreviewView) {
        healthFlashes -= healthBar
        healthBar.stop()
        healthBar.removeFromParent()
    }

    // Takes the health flashed off the tile, so it moves with its battle unit, which is about to leave it.
    private fun takeHealthFlash(position: TilePosition): BattlefieldHealthBarPreviewView? {
        val healthBar = tileOrNull(position.row, position.column)?.findViewByName(HEALTH_FLASH) as? BattlefieldHealthBarPreviewView ?: return null
        healthBar.removeFromParent()
        return healthBar
    }

    // Puts the health flashed that moved with its battle unit on its destination tile, unless it is over meanwhile, or the
    // tile displays a newer one, e.g.: the terrain of the destination changed its health while it moved there.
    private fun putHealthFlash(
        healthBar: BattlefieldHealthBarPreviewView,
        position: TilePosition,
    ) {
        if (healthBar !in healthFlashes) return
        val tileButton = tile(position.row, position.column)
        if (tileButton.findViewByName(HEALTH_FLASH) != null) return removeHealthFlash(healthBar)
        tileButton.addChild(healthBar)
    }

    /**
     * Darkens every tile but the given ones, including the battle units on them, so the tiles that can be selected
     * stand out. Removed when the tiles are reset.
     */
    open fun dimTilesExcept(tiles: Set<TilePosition>) {
        // The tiles not created yet are dimmed when they are created.
        val undimmed = undimmedTiles?.intersect(tiles) ?: tiles
        undimmedTiles = undimmed
        createdTiles.forEach { (position, tileButton) -> if (position !in undimmed) tileButton.dim() }
    }

    private fun UIButton.dim() {
        if (findViewByName(DIM) != null) return
        addChild(TileDimView().apply { name = DIM })
    }

    private fun UIButton.addHighlight(kind: TileHighlightView.Kind) {
        addChild(TileHighlightView(resourcesLoader, kind).apply { name = SELECTION })
    }

    /** Removes the npc standing on the tile, e.g.: once recruited. */
    open fun removeNpc(
        row: Int,
        column: Int,
    ) {
        tile(row, column).findViewByName(NPC)?.removeFromParent()
    }

    /** Removes the battle unit standing on the tile, stopping its flashes and its health flashed, e.g.: once defeated. */
    open fun removeBattleUnit(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(BATTLE_UNIT)?.removeFromParent()
        flashes.remove(TilePosition(row, column))?.forEach(Job::cancel)
        tileButton.findViewByName(FLASH)?.removeFromParent()
        tileButton.removeHealthFlash()
    }

    /** How the battlefield scrolls to follow a moving battle unit. */
    enum class Following {
        // Does not scroll, e.g.: a CPU battle unit wandering.
        NONE,

        // Before each step, centers the battle unit on each axis whose viewport edge it reaches, e.g.: in a battle.
        CENTER,

        // Trails the battle unit on every frame, without waiting for the scroll, e.g.: the leader of the party exploring.
        TRAIL,
    }

    /**
     * Glides the battle unit tile by tile along the path, then displays it on its destination tile, and scrolls the
     * battlefield so the battle unit stays visible, as following says. A step to a tile that is not next to the previous
     * one (e.g.: a teleport) jumps to it. The health flashed over the battle unit moves with it. Path: the tiles walked
     * from the origin (excluded) to the destination (included).
     */
    open suspend fun moveBattleUnit(
        fromRow: Int,
        fromColumn: Int,
        path: List<TilePosition>,
        unitId: String,
        stepDuration: Duration = MOVE_STEP_DURATION,
        following: Following = Following.CENTER,
    ) {
        val destination = path.lastOrNull() ?: return
        val origin = TilePosition(fromRow, fromColumn)
        val healthFlash = takeHealthFlash(origin)
        removeBattleUnit(fromRow, fromColumn)
        val glidingUnit =
            resourcesLoader.bitmapOrNull("unit/$unitId.png")?.let { unitBitmap ->
                movingBattleUnits.movingBattleUnit(unitBitmap, GLIDING_BATTLE_UNIT, origin.column * TILE_SIZE.toDouble(), origin.row * TILE_SIZE.toDouble())
            }
        healthFlash?.let { healthBar -> glidingUnit?.addChild(healthBar) }
        if (following == Following.TRAIL) trailFrom(origin, glidingUnit)
        var previousPosition = origin
        path.forEach { position ->
            if (following == Following.CENTER) followTile(position.row, position.column)
            glide(glidingUnit, previousPosition, position, stepDuration)
            previousPosition = position
        }
        if (following == Following.TRAIL) trailOn(destination, glidingUnit)
        glidingUnit?.removeFromParent()
        displayBattleUnit(destination.row, destination.column, unitId)
        healthFlash?.let { healthBar -> putHealthFlash(healthBar, destination) }
    }

    // Glides the image of the battle unit to the next tile of its path, or jumps to it when it is not next to the previous one.
    private suspend fun glide(
        glidingUnit: View?,
        from: TilePosition,
        to: TilePosition,
        stepDuration: Duration,
    ) {
        if (glidingUnit == null) return delay(stepDuration)
        val toX = to.column * TILE_SIZE.toDouble()
        val toY = to.row * TILE_SIZE.toDouble()
        if (abs(to.row - from.row) + abs(to.column - from.column) != 1) {
            glidingUnit.x = toX
            glidingUnit.y = toY
            return delay(stepDuration)
        }
        glidingUnit.animator { moveTo(glidingUnit, x = toX, y = toY, time = stepDuration, easing = Easing.LINEAR) }.awaitComplete()
    }

    /**
     * Swaps the tiles of the leader of the party and the party member on its next tile, trailing the leader as it
     * explores: both glide to each other's tile at once, the leader hops over the member, which slides straight
     * underneath. The health flashed over each of them moves with it.
     */
    open suspend fun swapBattleUnits(
        leaderRow: Int,
        leaderColumn: Int,
        leaderUnitId: String,
        memberRow: Int,
        memberColumn: Int,
        memberUnitId: String,
    ) {
        val leaderPosition = TilePosition(leaderRow, leaderColumn)
        val memberPosition = TilePosition(memberRow, memberColumn)
        trailFrom(leaderPosition, glidingUnit = null)
        val leaderHealthFlash = takeHealthFlash(leaderPosition)
        val memberHealthFlash = takeHealthFlash(memberPosition)
        removeBattleUnit(leaderRow, leaderColumn)
        removeBattleUnit(memberRow, memberColumn)
        glideSwap(leaderRow, leaderColumn, leaderUnitId, memberRow, memberColumn, memberUnitId, leaderHealthFlash, memberHealthFlash)
        // The leader glided with the image `glideSwap` trails, which is only known there.
        trailOn(memberPosition, glidingUnit = trailedView)
        displayBattleUnit(memberRow, memberColumn, leaderUnitId)
        displayBattleUnit(leaderRow, leaderColumn, memberUnitId)
        leaderHealthFlash?.let { healthBar -> putHealthFlash(healthBar, memberPosition) }
        memberHealthFlash?.let { healthBar -> putHealthFlash(healthBar, leaderPosition) }
    }

    private suspend fun glideSwap(
        leaderRow: Int,
        leaderColumn: Int,
        leaderUnitId: String,
        memberRow: Int,
        memberColumn: Int,
        memberUnitId: String,
        leaderHealthFlash: View?,
        memberHealthFlash: View?,
    ) {
        val leaderBitmap = resourcesLoader.bitmapOrNull("unit/$leaderUnitId.png")
        val memberBitmap = resourcesLoader.bitmapOrNull("unit/$memberUnitId.png")
        if (leaderBitmap == null || memberBitmap == null) return delay(SWAP_DURATION)
        val leaderX = leaderColumn * TILE_SIZE.toDouble()
        val leaderY = leaderRow * TILE_SIZE.toDouble()
        val memberX = memberColumn * TILE_SIZE.toDouble()
        val memberY = memberRow * TILE_SIZE.toDouble()
        val member = movingBattleUnits.movingBattleUnit(memberBitmap, SWAPPING_MEMBER, memberX, memberY)
        // Added last, so it is drawn over the member. Its image hops inside it, while it glides.
        val leader = movingBattleUnits.movingBattleUnit(leaderBitmap, SWAPPING_LEADER, leaderX, leaderY)
        if (isTrailing) trailedView = leader
        val leaderImage = leader.firstChild!!
        // Added after the images, so they glide over them, without hopping.
        memberHealthFlash?.let(member::addChild)
        leaderHealthFlash?.let(leader::addChild)
        movingBattleUnits
            .animator(parallel = true) {
                moveTo(member, x = leaderX, y = leaderY, time = SWAP_DURATION, easing = Easing.LINEAR)
                moveTo(leader, x = memberX, y = memberY, time = SWAP_DURATION, easing = Easing.LINEAR)
                sequence {
                    moveBy(leaderImage, y = -SWAP_HOP_HEIGHT, time = SWAP_DURATION / 2, easing = Easing.EASE_OUT_QUAD)
                    moveBy(leaderImage, y = SWAP_HOP_HEIGHT, time = SWAP_DURATION / 2, easing = Easing.EASE_IN_QUAD)
                }
            }.awaitComplete()
        member.removeFromParent()
        leader.removeFromParent()
    }

    // A battle unit gliding from the tile at the position, drawn as on its tile: its image fills the tile.
    private fun Container.movingBattleUnit(
        bitmap: Bitmap,
        viewName: String,
        x: Double,
        y: Double,
    ): Container =
        container {
            name = viewName
            this.x = x
            this.y = y
            addChild(UnitSpriteView(bitmap))
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
     * receives an effect. It blinks after the flashes already on its tile, so two silhouettes never blink at once, and
     * stops once the battle unit leaves its tile (e.g.: a step while exploring).
     */
    open suspend fun flashBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val unitBitmap = resourcesLoader.bitmapOrNull("unit/$unitId.png") ?: return
        followTile(row, column)
        val position = TilePosition(row, column)
        val tileButton = tile(row, column)
        val previousFlashes = flashes[position].orEmpty().toList()
        coroutineScope {
            val flash =
                launchImmediately {
                    previousFlashes.joinAll()
                    repeat(FLASH_COUNT) {
                        tileButton.addChild(TileFlashView(unitBitmap).apply { name = FLASH })
                        delay(FLASH_DURATION)
                        tileButton.findViewByName(FLASH)?.removeFromParent()
                        delay(FLASH_DURATION)
                    }
                }
            flashes.getOrPut(position) { mutableListOf() } += flash
            flash.invokeOnCompletion {
                val tileFlashes = flashes[position] ?: return@invokeOnCompletion
                tileFlashes -= flash
                if (tileFlashes.isEmpty()) flashes -= position
            }
        }
    }

    /** Scrolls the battlefield so the tile is visible, e.g.: the buyer of a merchant's shop. */
    open suspend fun scrollToTile(
        row: Int,
        column: Int,
    ) = followTile(row, column)

    /**
     * Trails the battle unit standing on the tile on every frame, e.g.: the leader of the party when the exploration
     * starts or resumes. The camera eases towards it when it is far from the deadzone, and is not waited for.
     */
    open fun trail(
        row: Int,
        column: Int,
    ) {
        trailedTile = TilePosition(row, column)
        trailedView = null
        isTrailingPaused = false
        isCatchingUp = true
        if (isDisplayed) battlefieldAnimator.cancel()
    }

    /** Stops trailing the leader, leaving the scroll where it is, e.g.: when a battle starts. */
    open fun stopTrailing() {
        trailedTile = null
        trailedView = null
    }

    // The leader steps from the tile, gliding with the image: the trailing starts, or resumes after a drag.
    private fun trailFrom(
        position: TilePosition,
        glidingUnit: View?,
    ) {
        if (!isTrailing || isTrailingPaused) trail(position.row, position.column) else trailedTile = position
        trailedView = glidingUnit
    }

    // The leader stands on the tile once it has glided there, unless the trailing stopped meanwhile.
    private fun trailOn(
        position: TilePosition,
        glidingUnit: View?,
    ) {
        if (!isTrailing || trailedView !== glidingUnit) return
        trailedTile = position
        trailedView = null
    }

    // Keeps the leader inside the deadzone: straight away while it walks, easing towards it when it is far.
    private fun trailLeader(dt: Duration) {
        val tile = trailedTile ?: return
        if (isTrailingPaused) return
        val trailedX = trailedView?.x ?: (tile.column * TILE_SIZE.toDouble())
        val trailedY = trailedView?.y ?: (tile.row * TILE_SIZE.toDouble())
        val targetX = trailedScroll(trailedX, battlefieldGrid.x, minScrollX)
        val targetY = trailedScroll(trailedY, battlefieldGrid.y, minScrollY)
        if (abs(targetX - battlefieldGrid.x) > TILE_SIZE || abs(targetY - battlefieldGrid.y) > TILE_SIZE) isCatchingUp = true
        if (isCatchingUp) {
            battlefieldGrid.x = catchUp(battlefieldGrid.x, targetX, dt)
            battlefieldGrid.y = catchUp(battlefieldGrid.y, targetY, dt)
            isCatchingUp = battlefieldGrid.x != targetX || battlefieldGrid.y != targetY
        } else {
            battlefieldGrid.x = targetX
            battlefieldGrid.y = targetY
        }
    }

    // The scroll of one axis keeping the trailed offset inside the deadzone, clamped to the battlefield. At a whole pixel,
    // as the scroll is drawn there anyway (e.g.: while the leader glides).
    private fun trailedScroll(
        trailedOffset: Double,
        scrollOffset: Double,
        minScroll: Double,
    ): Double {
        val trailedOffsetInViewport = trailedOffset + scrollOffset
        val trailedScroll =
            when {
                trailedOffsetInViewport < DEADZONE_START -> DEADZONE_START - trailedOffset
                trailedOffsetInViewport > DEADZONE_END -> DEADZONE_END - trailedOffset
                else -> scrollOffset
            }
        return round(trailedScroll).clamp(minScroll, 0.0)
    }

    // Moves by whole pixels, at least one, so the scroll drawn at whole pixels still reaches the target.
    private fun catchUp(
        scrollOffset: Double,
        targetOffset: Double,
        dt: Duration,
    ): Double {
        val distance = targetOffset - scrollOffset
        val eased = abs(distance) * (1 - exp(-(dt / CATCH_UP_TIME)))
        val step = ceil(max(eased, CATCH_UP_MIN_SPEED * dt.toDouble(DurationUnit.SECONDS)))
        return if (step >= abs(distance)) targetOffset else scrollOffset + sign(distance) * step
    }

    /**
     * Puts the views moving on the battlefield back on whole pixels, once they moved on this frame, so every art pixel is
     * drawn with the same number of screen pixels: the scroll, the gliding battle units (and the image of the one hopping)
     * and the rising loot. Their animations go on from where they started, so rounding them does not slow them down.
     */
    private fun drawAtWholePixels() {
        battlefieldGrid.roundPosition()
        movingBattleUnits.forEachChild { glidingUnit ->
            glidingUnit.roundPosition()
            glidingUnit.forEachChild { image -> image.roundPosition() }
        }
        risingLoot.forEach { pickedUpView -> pickedUpView.roundPosition() }
    }

    private fun View.roundPosition() {
        x = round(x)
        y = round(y)
    }

    // Scrolls each axis where the tile is out of the viewport or on its edge, centering it, and keeps the scroll of the
    // other axis. On a border of the battlefield, the scroll is clamped back to the current one, so that axis does not
    // scroll. While trailing, the camera belongs to the leader, so it does not scroll.
    private suspend fun followTile(
        row: Int,
        column: Int,
    ) {
        if (isTrailing) return
        val followedX = followedScroll(column, battlefieldGrid.x, VIEWPORT_WIDTH, minScrollX)
        val followedY = followedScroll(row, battlefieldGrid.y, VIEWPORT_HEIGHT, minScrollY)
        if (followedX == battlefieldGrid.x && followedY == battlefieldGrid.y) return
        scrollTo(x = followedX, y = followedY)
        battlefieldAnimator.awaitComplete()
    }

    // The scroll of one axis: kept while the tile is away from the viewport edges, else centering it.
    private fun followedScroll(
        tileIndex: Int,
        scrollOffset: Double,
        viewportSize: Int,
        minScroll: Double,
    ): Double {
        val tileOffset = tileIndex * TILE_SIZE.toDouble()
        if (isAwayFromViewportEdges(tileOffset, scrollOffset, viewportSize)) return scrollOffset
        return ((viewportSize - TILE_SIZE) / 2.0 - tileOffset).clamp(minScroll, 0.0)
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

    /** Draws the image centered on the tile, by default scaled to fill it, whatever the size of its art (16x16 or 32x32). */
    private fun UIButton.addImage(
        bitmap: Bitmap,
        viewName: String,
        scale: Double = tileFillingScale(bitmap),
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
}
