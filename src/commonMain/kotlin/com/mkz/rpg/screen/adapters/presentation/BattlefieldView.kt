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
    private val resourcesLoader: GameSceneResourcesLoader,
) : UIContainer(Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT)) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val DROPPED_ITEMS = "DROPPED_ITEMS"
        const val LOOTED_ITEM = "LOOTED_ITEM"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val NPC = "NPC"
        const val QUEST_MARKER = "QUEST_MARKER"
        const val BUILDING = "BUILDING"
        const val SELECTION = "SELECTION"
        const val BLINK = "BLINK"
        const val DIM = "DIM"
        const val HEALTH_PREVIEW = "HEALTH_PREVIEW"
        const val HEALTH_BLINK = "HEALTH_BLINK"
        const val BATTLEFIELD = "BATTLEFIELD"
        const val MOVING_BATTLE_UNITS = "MOVING_BATTLE_UNITS"
        const val GLIDING_BATTLE_UNIT = "GLIDING_BATTLE_UNIT"
        const val SWAPPING_LEADER = "SWAPPING_LEADER"
        const val SWAPPING_MEMBER = "SWAPPING_MEMBER"

        const val TILE_WINDOW_MARGIN = 1

        private val KEPT_LAYERS = setOf(BUILDING, DROPPED_ITEMS, BATTLE_UNIT, NPC, QUEST_MARKER, SELECTION, HEALTH_PREVIEW, BLINK, HEALTH_BLINK, LOOTED_ITEM)

        private val ANIMATED_LAYERS = setOf(BLINK, HEALTH_BLINK, LOOTED_ITEM)

        const val TILE_SIZE = 64
        const val TILE_PIXEL_SIZE = 16
        const val VISIBLE_TILES = 6
        const val VIEWPORT_WIDTH = TILE_SIZE * VISIBLE_TILES
        const val VIEWPORT_HEIGHT = TILE_SIZE * VISIBLE_TILES

        const val DROPPED_ITEM_SCALE = 2.0
        const val CHEST_PATH = "battlefield/chest.png"

        const val LOOTED_ITEM_RISE = TILE_SIZE / 2.0
        const val LOOTED_ITEM_SCALE = 3.0
        val LOOTED_ITEM_DURATION = 400.milliseconds

        val MOVE_STEP_DURATION = 150.milliseconds

        val EXPLORING_STEP_DURATION = 250.milliseconds

        val WANDERING_STEP_DURATION = 75.milliseconds
        val SCROLL_DURATION = 300.milliseconds

        const val DEADZONE_START = TILE_SIZE * (VISIBLE_TILES / 2 - 1)
        const val DEADZONE_END = TILE_SIZE * (VISIBLE_TILES / 2)

        val CATCH_UP_TIME = 80.milliseconds
        const val CATCH_UP_MIN_SPEED = TILE_SIZE * 8.0

        val SWAP_DURATION = 150.milliseconds
        const val SWAP_HOP_HEIGHT = (TILE_SIZE / 3).toDouble()

        const val DIM_ALPHA = 0.4

        const val BLINK_COUNT = 3
        val BLINK_DURATION = 80.milliseconds

        val HEALTH_BLINK_DURATION = 1500.milliseconds
        val HEALTH_BLINK_FADE_DURATION = 200.milliseconds

        const val TINT_ALPHA = 0.4
        val TINT_DURATION = 500.milliseconds

        internal fun tileFillingScale(bitmap: Bitmap): Double = TILE_SIZE.toDouble() / bitmap.width
    }

    private var delegate: Delegate? = null

    private val terrainBitmaps = TerrainBitmaps(resourcesLoader)
    internal lateinit var battlefieldAnimator: Animator

    private val blinks = mutableMapOf<TilePosition, MutableList<Job>>()

    private val healthBlinks = mutableSetOf<BattlefieldHealthBarPreviewView>()

    private val risingLoot = mutableSetOf<View>()

    private var minScrollX = 0.0
    private var minScrollY = 0.0

    private val viewport =
        clipContainer(
            size = Size(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
        )

    internal lateinit var battlefieldGrid: Container

    internal lateinit var movingBattleUnits: Container

    private lateinit var camera: View
    internal var tintOverlay: SolidRect? = null

    private var trailedTile: TilePosition? = null
    private var trailedView: View? = null

    private var isTrailingPaused = false

    private var isCatchingUp = false

    private val isTrailing: Boolean
        get() = trailedTile != null

    private lateinit var battlefield: BattlefieldTerrain
    internal val rows: Int get() = battlefield.rows
    internal val columns: Int get() = battlefield.columns

    private val createdTiles = mutableMapOf<TilePosition, UIButton>()

    private var undimmedTiles: Set<TilePosition>? = null

    private var windowScroll: Pair<Double, Double>? = null

    init {
        addChild(viewport)
        var dragStartX = 0.0
        var dragStartY = 0.0
        viewport.onMouseDrag { event ->
            if (!isDisplayed) return@onMouseDrag
            if (event.start) {
                isTrailingPaused = isTrailing
                dragStartX = battlefieldGrid.x
                dragStartY = battlefieldGrid.y
            }
            val drag = event.localDXY(battlefieldGrid)
            scrollTo(x = dragStartX + drag.x, y = dragStartY + drag.y)
        }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    open fun displayBattlefield(battlefield: BattlefieldTerrain) {
        val battlefieldHeight = TILE_SIZE * battlefield.rows
        val battlefieldWidth = TILE_SIZE * battlefield.columns
        val previousScroll = if (isDisplayed) battlefieldGrid.x to battlefieldGrid.y else 0.0 to 0.0
        if (isDisplayed) {
            battlefieldAnimator.cancel()
            createdTiles.values.forEach(::removeHealthPreview)
            removeHealthBlinks()
            battlefieldGrid.removeFromParent()
            movingBattleUnits.removeFromParent()
            camera.removeFromParent()
        }
        createdTiles.clear()
        undimmedTiles = null
        windowScroll = null
        trailedView = null
        this.battlefield = battlefield
        minScrollX = minOf(0, VIEWPORT_WIDTH - battlefieldWidth).toDouble()
        minScrollY = minOf(0, VIEWPORT_HEIGHT - battlefieldHeight).toDouble()
        battlefieldGrid =
            viewport.container {
                name = BATTLEFIELD
                x = previousScroll.first.clamp(minScrollX, 0.0)
                y = previousScroll.second.clamp(minScrollY, 0.0)
            }
        battlefieldAnimator = battlefieldGrid.animator(parallel = true)
        movingBattleUnits = viewport.container { name = MOVING_BATTLE_UNITS }
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

    private fun refreshTileWindow() {
        val scroll = battlefieldGrid.x to battlefieldGrid.y
        if (scroll == windowScroll) return
        windowScroll = scroll
        val createdTilesBeforeRemovals = createdTiles.toList()
        createdTilesBeforeRemovals.forEach { (position, tileButton) ->
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
        battlefieldGrid.sortChildrenBy(compareBy<View>({ view -> view.y }, { view -> view.x }))
    }

    private fun UIButton.hasLayerIn(layers: Set<String>): Boolean = children.any { child -> child.name in layers }

    private fun createTile(position: TilePosition): UIButton {
        val tileButton = UIButton(size = Size(TILE_SIZE, TILE_SIZE), text = "")
        tileButton.bgColorOut = Colors.TRANSPARENT
        tileButton.bgColorOver = Colors.TRANSPARENT
        tileButton.background.borderColor = Colors.TRANSPARENT
        tileButton.background.bgColor = Colors.TRANSPARENT
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

    open fun terrainTileBitmap(
        terrainId: String,
        terrainTransition: TerrainTransition?,
    ): Bitmap = terrainBitmaps.bitmap(terrainId, terrainTransition)

    internal val isDisplayed: Boolean
        get() = ::battlefieldGrid.isInitialized

    internal val tiles: List<UIButton>
        get() = createdTiles.values.toList()

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

    open fun displayNpc(
        row: Int,
        column: Int,
        npcId: String,
        unitId: String?,
    ) {
        val npcBitmap = resourcesLoader.bitmapOrNull(unitId?.let { "unit/$it.png" } ?: "npc/$npcId.png") ?: return
        tile(row, column).addChild(UnitSpriteView(npcBitmap).apply { name = NPC })
    }

    open fun displayQuestMarker(
        row: Int,
        column: Int,
        sign: QuestMarkerView.Sign,
    ) {
        hideQuestMarker(row, column)
        tile(row, column).addChild(QuestMarkerView(sign).apply { name = QUEST_MARKER })
    }

    open fun hideQuestMarker(
        row: Int,
        column: Int,
    ) {
        tile(row, column).findViewByName(QUEST_MARKER)?.removeFromParent()
    }

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

    open fun displayDroppedItems(
        row: Int,
        column: Int,
        lootableItemIds: List<String>,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(DROPPED_ITEMS)?.removeFromParent()
        val lootBitmap =
            when (lootableItemIds.size) {
                0 -> return
                1 -> resourcesLoader.bitmapOrNull("lootableItem/${lootableItemIds.single()}.png") ?: return
                else -> resourcesLoader.bitmap(CHEST_PATH)
            }
        val terrainIndex = tileButton.children.indexOfFirst { child -> child.name == TERRAIN }
        tileButton.addImage(lootBitmap, DROPPED_ITEMS, scale = DROPPED_ITEM_SCALE)
        val lootView = tileButton.findViewByName(DROPPED_ITEMS)!!
        lootView.removeFromParent()
        tileButton.addChildAt(lootView, terrainIndex + 1)
    }

    open suspend fun lootDroppedItem(
        row: Int,
        column: Int,
        lootableItemId: String,
        remainingLootableItemIds: List<String>,
    ) {
        displayDroppedItems(row = row, column = column, lootableItemIds = remainingLootableItemIds)
        val itemBitmap = resourcesLoader.bitmapOrNull("lootableItem/$lootableItemId.png") ?: return
        followTile(row, column)
        val pickedUpView =
            tile(row, column).image(itemBitmap) {
                name = LOOTED_ITEM
                scale = DROPPED_ITEM_SCALE
                smoothing = false
                anchor(Anchor.CENTER)
                x = TILE_SIZE / 2.0
                y = TILE_SIZE / 2.0
            }
        risingLoot += pickedUpView
        try {
            pickedUpView
                .animator(parallel = true) {
                    moveBy(pickedUpView, x = 0.0, y = -LOOTED_ITEM_RISE, time = LOOTED_ITEM_DURATION, easing = Easing.EASE_OUT_QUAD)
                    scaleTo(pickedUpView, LOOTED_ITEM_SCALE, time = LOOTED_ITEM_DURATION)
                    alpha(pickedUpView, 0.0, time = LOOTED_ITEM_DURATION, easing = Easing.EASE_IN_QUAD)
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
        tileButton.addHighlight(TileHighlightView.Kind.WALKING_RANGE)
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

    open suspend fun blinkHealth(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        val tileButton = tile(row, column)
        tileButton.removeHealthBlink()
        val healthBar = BattlefieldHealthBarPreviewView(Size(TILE_SIZE, TILE_SIZE / 5.0))
        healthBar.name = HEALTH_BLINK
        healthBar.display(previewOption)
        tileButton.addChild(healthBar)
        healthBlinks += healthBar
        val blinkDuration = BLINK_DURATION * BLINK_COUNT * 2
        delay(blinkDuration)
        if (healthBar !in healthBlinks) return
        healthBar.hold()
        delay(HEALTH_BLINK_DURATION - blinkDuration - HEALTH_BLINK_FADE_DURATION)
        if (healthBar !in healthBlinks) return
        animator { alpha(healthBar, 0.0, time = HEALTH_BLINK_FADE_DURATION) }.awaitComplete()
        if (healthBar in healthBlinks) removeHealthBlink(healthBar)
    }

    open fun removeHealthBlinks() {
        healthBlinks.toList().forEach(::removeHealthBlink)
    }

    private fun UIButton.removeHealthBlink() {
        val healthBar = findViewByName(HEALTH_BLINK) as? BattlefieldHealthBarPreviewView ?: return
        removeHealthBlink(healthBar)
    }

    private fun removeHealthBlink(healthBar: BattlefieldHealthBarPreviewView) {
        healthBlinks -= healthBar
        healthBar.stop()
        healthBar.removeFromParent()
    }

    private fun takeHealthBlink(position: TilePosition): BattlefieldHealthBarPreviewView? {
        val healthBar = tileOrNull(position.row, position.column)?.findViewByName(HEALTH_BLINK) as? BattlefieldHealthBarPreviewView ?: return null
        healthBar.removeFromParent()
        return healthBar
    }

    private fun putHealthBlink(
        healthBar: BattlefieldHealthBarPreviewView,
        position: TilePosition,
    ) {
        if (healthBar !in healthBlinks) return
        val tileButton = tile(position.row, position.column)
        if (tileButton.findViewByName(HEALTH_BLINK) != null) return removeHealthBlink(healthBar)
        tileButton.addChild(healthBar)
    }

    open fun dimTilesExcept(tiles: Set<TilePosition>) {
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

    open fun removeNpc(
        row: Int,
        column: Int,
    ) {
        tile(row, column).findViewByName(NPC)?.removeFromParent()
    }

    open fun removeBattleUnit(
        row: Int,
        column: Int,
    ) {
        val tileButton = tile(row, column)
        tileButton.findViewByName(BATTLE_UNIT)?.removeFromParent()
        blinks.remove(TilePosition(row, column))?.forEach(Job::cancel)
        tileButton.findViewByName(BLINK)?.removeFromParent()
        tileButton.removeHealthBlink()
    }

    enum class Following {
        NONE,

        CENTER,

        TRAIL,
    }

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
        val healthBlink = takeHealthBlink(origin)
        removeBattleUnit(fromRow, fromColumn)
        val glidingUnit =
            resourcesLoader.bitmapOrNull("unit/$unitId.png")?.let { unitBitmap ->
                movingBattleUnits.movingBattleUnit(unitBitmap, GLIDING_BATTLE_UNIT, origin.column * TILE_SIZE.toDouble(), origin.row * TILE_SIZE.toDouble())
            }
        healthBlink?.let { healthBar -> glidingUnit?.addChild(healthBar) }
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
        healthBlink?.let { healthBar -> putHealthBlink(healthBar, destination) }
    }

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
        val leaderHealthBlink = takeHealthBlink(leaderPosition)
        val memberHealthBlink = takeHealthBlink(memberPosition)
        removeBattleUnit(leaderRow, leaderColumn)
        removeBattleUnit(memberRow, memberColumn)
        glideSwap(leaderRow, leaderColumn, leaderUnitId, memberRow, memberColumn, memberUnitId, leaderHealthBlink, memberHealthBlink)
        trailOn(memberPosition, glidingUnit = trailedView)
        displayBattleUnit(memberRow, memberColumn, leaderUnitId)
        displayBattleUnit(leaderRow, leaderColumn, memberUnitId)
        leaderHealthBlink?.let { healthBar -> putHealthBlink(healthBar, memberPosition) }
        memberHealthBlink?.let { healthBar -> putHealthBlink(healthBar, leaderPosition) }
    }

    private suspend fun glideSwap(
        leaderRow: Int,
        leaderColumn: Int,
        leaderUnitId: String,
        memberRow: Int,
        memberColumn: Int,
        memberUnitId: String,
        leaderHealthBlink: View?,
        memberHealthBlink: View?,
    ) {
        val leaderBitmap = resourcesLoader.bitmapOrNull("unit/$leaderUnitId.png")
        val memberBitmap = resourcesLoader.bitmapOrNull("unit/$memberUnitId.png")
        if (leaderBitmap == null || memberBitmap == null) return delay(SWAP_DURATION)
        val leaderX = leaderColumn * TILE_SIZE.toDouble()
        val leaderY = leaderRow * TILE_SIZE.toDouble()
        val memberX = memberColumn * TILE_SIZE.toDouble()
        val memberY = memberRow * TILE_SIZE.toDouble()
        val member = movingBattleUnits.movingBattleUnit(memberBitmap, SWAPPING_MEMBER, memberX, memberY)
        val leader = movingBattleUnits.movingBattleUnit(leaderBitmap, SWAPPING_LEADER, leaderX, leaderY)
        if (isTrailing) trailedView = leader
        val leaderImage = leader.firstChild!!
        memberHealthBlink?.let(member::addChild)
        leaderHealthBlink?.let(leader::addChild)
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

    open suspend fun blinkBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val unitBitmap = resourcesLoader.bitmapOrNull("unit/$unitId.png") ?: return
        followTile(row, column)
        val position = TilePosition(row, column)
        val tileButton = tile(row, column)
        val previousBlinks = blinks[position].orEmpty().toList()
        coroutineScope {
            val blink =
                launchImmediately {
                    previousBlinks.joinAll()
                    repeat(BLINK_COUNT) {
                        tileButton.addChild(TileBlinkView(unitBitmap).apply { name = BLINK })
                        delay(BLINK_DURATION)
                        tileButton.findViewByName(BLINK)?.removeFromParent()
                        delay(BLINK_DURATION)
                    }
                }
            blinks.getOrPut(position) { mutableListOf() } += blink
            blink.invokeOnCompletion {
                val tileBlinks = blinks[position] ?: return@invokeOnCompletion
                tileBlinks -= blink
                if (tileBlinks.isEmpty()) blinks -= position
            }
        }
    }

    open suspend fun scrollToTile(
        row: Int,
        column: Int,
    ) = followTile(row, column)

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

    open fun stopTrailing() {
        trailedTile = null
        trailedView = null
    }

    private fun trailFrom(
        position: TilePosition,
        glidingUnit: View?,
    ) {
        if (!isTrailing || isTrailingPaused) trail(position.row, position.column) else trailedTile = position
        trailedView = glidingUnit
    }

    private fun trailOn(
        position: TilePosition,
        glidingUnit: View?,
    ) {
        if (!isTrailing || trailedView !== glidingUnit) return
        trailedTile = position
        trailedView = null
    }

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
