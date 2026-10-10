package com.mkz.rpg.screen

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionDto
import com.mkz.rpg.screen.feedback.FxLayer
import com.mkz.rpg.screen.feedback.FxViews
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.snapToArtPixel
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
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
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.View
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.clipContainer
import korlibs.korge.view.image
import korlibs.math.clamp
import korlibs.math.geom.Size
import korlibs.math.geom.Spacing
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot

class BattlefieldView(
    private val sprites: SpriteRegistry = SpriteRegistry(),
    private val viewportSize: Size = Size(width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT),
    /** How long each idle frame is shown. Zero keeps standing units on frame 0, which keeps snapshots deterministic. */
    private val idleFrameMs: Int = 0,
) : UIContainer(viewportSize) {
    companion object {
        const val TERRAIN = "TERRAIN"
        const val BATTLE_UNIT = "BATTLE_UNIT"
        const val SELECTION = "SELECTION"
        const val UNIT_OVERLAY = "UNIT_OVERLAY"
        const val INSPECT_GLYPH = "INSPECT_GLYPH"
        const val DIM = "DIM"
        const val CONDITIONAL = "CONDITIONAL"
        const val CONNECTOR = "CONNECTOR"

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
    private var variantBitMaps: Map<String, List<Bitmap>> = emptyMap()
    private var mapWidth = 0.0
    private var mapHeight = 0.0
    private var mapRows = 0
    private var mapColumns = 0
    private var scrollX = 0.0
    private var scrollY = 0.0
    private var gestureIsDrag = false

    private val viewport = clipContainer(size = viewportSize)
    private lateinit var battlefieldGrid: UIGridFill
    private val fxLayer = FxLayer()
    private val walkers = mutableMapOf<String, View>()
    private val walkerSteps = mutableMapOf<String, Int>()
    private val idleImages = mutableMapOf<Image, List<Bitmap>>()
    private var idleClockMs = 0.0
    private var idleFrame = 0

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
        variantBitMaps = loadTerrainVariants()
        sprites.load()
    }

    /** Alternate base tiles per terrain. They live outside `transitions/`, so they never add a terrain rule. */
    private suspend fun loadTerrainVariants(): Map<String, List<Bitmap>> {
        val variantsRoot = resourcesVfs["terrain/variants"]
        if (!variantsRoot.exists()) return emptyMap()
        return variantsRoot
            .listNames()
            .filter { it.endsWith(".png") }
            .associate { fileName ->
                val strip = variantsRoot[fileName].readBitmap()
                fileName.removeSuffix(".png") to
                    List(strip.width / TILE_PIXEL_SIZE) { index -> strip.crop(index * TILE_PIXEL_SIZE, 0, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE) }
            }
    }

    fun displayBattlefield(battlefield: Battlefield.Dto) {
        mapRows = battlefield.rows
        mapColumns = battlefield.columns
        mapHeight = (TILE_SIZE * battlefield.rows).toDouble()
        mapWidth = (TILE_SIZE * battlefield.columns).toDouble()
        battlefieldGrid =
            viewport.uiGridFill(
                size = Size(width = mapWidth, height = mapHeight),
                spacing = Spacing(0.0, 0.0),
                cols = 0,
                rows = 0,
            )
        viewport.addChild(fxLayer)
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
                    tileButton.addImage(terrainTileBitmap(tileDto.terrainId, tileDto.terrainTransition, row, column), TERRAIN)
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
        fxLayer.x = battlefieldGrid.x
        fxLayer.y = battlefieldGrid.y
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

    /**
     * The tile drawn at [row], [column]. Transition tiles come from their strip. Base tiles use a variant picked from the
     * position, so the same tile always looks the same and the 16 px repeat is broken up.
     */
    fun terrainTileBitmap(
        terrainId: String,
        terrainTransition: TerrainTransitionDto?,
        row: Int = 0,
        column: Int = 0,
    ): Bitmap {
        val transition = terrainTransition
        if (transition != null) {
            val transitionName = "${transition.fromTerrainId}$TRANSITION_NAME_SEPARATOR${transition.toTerrainId}"
            return transitionBitMaps.getValue(transitionName)[transition.wangIndex]
        }
        val base = terrainBitMaps[terrainId] ?: throw IllegalStateException("Terrain $terrainId bitmap not found")
        val variants = variantBitMaps[terrainId].orEmpty()
        val pick = terrainVariantPick(row, column)
        return if (pick in 1..variants.size) variants[pick - 1] else base
    }

    /** 0..7 from the position; 1..N selects variant N - 1, anything else keeps the base tile (so most tiles stay base). */
    private fun terrainVariantPick(
        row: Int,
        column: Int,
    ): Int = abs((row * 73_856_093) xor (column * 19_349_663)) % 8

    private fun tileName(
        row: Int,
        column: Int,
    ): String = "row-$row-column-$column"

    fun displayKnightBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, "knight")
    }

    fun displayRatBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, "rat")
    }

    fun displayBeeBattleUnit(
        row: Int,
        column: Int,
    ) {
        displayUnit(row, column, "bee")
    }

    private fun displayUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        val frames = sprites.unitFrames(unitId, SpriteRegistry.IDLE)
        val image = tileButton.addImage(frames[idleFrame % frames.size], BATTLE_UNIT)
        if (frames.size > 1) idleImages[image] = frames
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
        kind: CastTargetKind = CastTargetKind.TARGET_TILE,
        groupEdges: Set<CastEdge> = emptySet(),
        lethal: Boolean = false,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        if (tileButton.findViewByName(SELECTION) != null) return
        tileButton.addChild(CastTargetTileView(kind, groupEdges, lethal).also { it.name = SELECTION })
    }

    /** Outlines a unit that would receive a spread status when a previewed target is defeated. */
    fun displayConditionalTile(
        row: Int,
        column: Int,
        strong: Boolean,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addChild(ConditionalTileView(strong).also { it.name = CONDITIONAL })
    }

    /** Draws half of a spread connector on the tile, towards [edge]. */
    fun displaySpreadConnector(
        row: Int,
        column: Int,
        edge: CastEdge,
        arrowhead: Boolean,
        strong: Boolean,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.addChild(SpreadConnectorView(edge, arrowhead, strong).also { it.name = CONNECTOR })
    }

    /** Dims every tile that isn't in [validTiles], so valid and invalid tiles differ by pattern and not only by color. */
    fun dimOutside(validTiles: Set<Pair<Int, Int>>) {
        clearDim()
        for (row in 0 until mapRows) {
            for (column in 0 until mapColumns) {
                if ((row to column) in validTiles) continue
                (battlefieldGrid.findViewByName(tileName(row, column)) as? UIButton)?.addChild(DimTileView().also { it.name = DIM })
            }
        }
    }

    fun clearDim() {
        battlefieldGrid.children.forEach { view ->
            (view as UIButton).findViewByName(DIM)?.removeFromParent()
        }
    }

    /** Marks a tile affected by the previewed cast with a reticle, replacing the valid-target border it had. */
    fun displayCastPreviewTile(
        row: Int,
        column: Int,
    ) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.findViewByName(SELECTION)?.removeFromParent()
        tileButton.addChild(CastPreviewTileView().also { it.name = SELECTION })
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
            tileButton.findViewByName(DIM)?.removeFromParent()
            tileButton.children.filter { it.name == CONDITIONAL || it.name == CONNECTOR }.forEach { it.removeFromParent() }
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

    // Feedback effects. They are drawn in a layer over the grid that scrolls with it, and never touch the tiles.

    /** Number of feedback effects still playing. */
    val activeFxCount: Int get() = fxLayer.activeCount

    fun advanceFx(deltaMs: Double) {
        fxLayer.advance(deltaMs)
        advanceIdle(deltaMs)
    }

    /** Steps every standing unit through its idle frames. All units share one clock, so they move together. */
    private fun advanceIdle(deltaMs: Double) {
        if (idleFrameMs <= 0) return
        idleClockMs += deltaMs
        val frame = (idleClockMs / idleFrameMs).toInt()
        if (frame == idleFrame) return
        idleFrame = frame
        idleImages.keys.removeAll { it.parent == null }
        idleImages.forEach { (image, frames) -> image.bitmap = frames[frame % frames.size].slice() }
    }

    fun clearFx() {
        fxLayer.clearEffects()
        walkers.values.forEach { it.removeFromParent() }
        walkers.clear()
        walkerSteps.clear()
    }

    /** The unit sprite turns white for a moment. */
    fun playHitFlash(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        val flash = FxViews.flash(sprites.silhouette(unitId)).also { it.name = FxViews.FLASH_NAME }
        placeOnTile(flash, row, column)
        fxLayer.play(flash, FxViews.FLASH_MS)
    }

    /** A damage or heal number pops above the unit and rises in whole art pixels. */
    fun playAmountPop(
        row: Int,
        column: Int,
        amount: Int,
        heal: Boolean,
    ) {
        val number = FxViews.amountNumber(amount, heal)
        val left = (TILE_PIXEL_SIZE - FxViews.amountWidthArtPixels(amount, heal)) / 2
        val startY = 2
        placeOnTile(number, row, column, artX = left, artY = startY)
        fxLayer.play(number, FxViews.NUMBER_MS) { progress ->
            number.y = tileY(row) + (startY - FxViews.steps(progress, FxViews.NUMBER_RISE_ART_PIXELS)) * PIXEL_SCALE.toDouble()
        }
    }

    /** Venom bubbles rising over the unit (authored `fx_venom` strip), or a sparkle at its status pips when the strip is missing. */
    fun playStatusPop(
        row: Int,
        column: Int,
    ) {
        val authoredFrames = authoredStrip(SpriteRegistry.FX_VENOM, FxViews.VENOM_FRAMES)
        if (authoredFrames != null) {
            val pop = Container().also { it.name = FxViews.POP_NAME }
            placeOnTile(pop, row, column)
            var shownFrame = -1
            fxLayer.play(pop, FxViews.POP_MS) { progress ->
                val frame = FxViews.steps(progress, FxViews.VENOM_FRAMES - 1)
                if (frame != shownFrame) {
                    shownFrame = frame
                    pop.showFrame(authoredFrames[frame])
                }
            }
            return
        }
        val pop = FxViews.popFrame(0).also { it.name = FxViews.POP_NAME }
        placeOnTile(pop, row, column, artX = 0, artY = 0)
        var shownFrame = 0
        fxLayer.play(pop, FxViews.POP_MS) { progress ->
            val frame = FxViews.steps(progress, 2)
            if (frame != shownFrame || progress == 0.0) {
                shownFrame = frame
                pop.removeChildren()
                FxViews
                    .popFrame(frame)
                    .children
                    .toList()
                    .forEach { pop.addChild(it) }
            }
        }
    }

    /** The defeat effect over the tile of a unit that was just defeated: the authored `fx_defeat` strip when present, else an expanding ring. */
    fun playPoof(
        row: Int,
        column: Int,
    ) {
        val poof = Container().also { it.name = FxViews.POOF_NAME }
        val authoredFrames = authoredStrip(SpriteRegistry.FX_DEFEAT, FxViews.POOF_FRAMES)
        placeOnTile(poof, row, column)
        var shownFrame = -1
        fxLayer.play(poof, FxViews.POOF_MS) { progress ->
            val frame = FxViews.steps(progress, FxViews.POOF_FRAMES - 1)
            if (frame != shownFrame) {
                shownFrame = frame
                if (authoredFrames != null) {
                    poof.showFrame(authoredFrames[frame])
                } else {
                    poof.removeChildren()
                    FxViews
                        .poofFrame(frame)
                        .children
                        .toList()
                        .forEach { poof.addChild(it) }
                }
            }
        }
    }

    /** A slash across the tile of a unit that took damage, played with the hit flash. Needs the authored `fx_hit` strip. */
    fun playHitSlash(
        row: Int,
        column: Int,
    ) {
        playTileStrip(SpriteRegistry.FX_HIT, FxViews.HIT_FRAMES, FxViews.FLASH_MS, FxViews.HIT_NAME, row, column)
    }

    /** Plus signs rising over a unit that was healed. Needs the authored `fx_heal` strip. */
    fun playHealSparkle(
        row: Int,
        column: Int,
    ) {
        playTileStrip(SpriteRegistry.FX_HEAL, FxViews.HEAL_FRAMES, FxViews.HEAL_MS, FxViews.HEAL_NAME, row, column)
    }

    private fun playTileStrip(
        fxId: String,
        frameCount: Int,
        durationMs: Int,
        viewName: String,
        row: Int,
        column: Int,
    ) {
        val frames = authoredStrip(fxId, frameCount) ?: return
        val view = Container().also { it.name = viewName }
        placeOnTile(view, row, column)
        var shownFrame = -1
        fxLayer.play(view, durationMs) { progress ->
            val frame = FxViews.steps(progress, frameCount - 1)
            if (frame != shownFrame) {
                shownFrame = frame
                view.showFrame(frames[frame])
            }
        }
    }

    private fun authoredStrip(
        fxId: String,
        frameCount: Int,
    ): List<Bitmap>? = sprites.fxFrames(fxId)?.takeIf { it.size == frameCount }

    private fun Container.showFrame(frame: Bitmap) {
        removeChildren()
        addChild(
            Image(frame).also {
                it.scale = PIXEL_SCALE.toDouble()
                it.smoothing = false
            },
        )
    }

    /**
     * A venom glob (authored `fx_spread` strip, wobbling between its frames) or a spark when the strip is missing travels from
     * the center of one tile to the center of another, moving in whole art pixels.
     */
    fun playSpark(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
        durationMs: Int,
    ) {
        val authoredFrames = authoredStrip(SpriteRegistry.FX_SPREAD, FxViews.SPREAD_FRAMES)
        val spark = (if (authoredFrames != null) Container() else FxViews.spark()).also { it.name = FxViews.SPARK_NAME }
        // The authored glob is drawn on a whole tile, so its center is half a tile from its corner.
        val offset = if (authoredFrames != null) TILE_SIZE / 2.0 else 0.0
        val startX = fromColumn * TILE_SIZE + TILE_SIZE / 2.0 - offset
        val startY = fromRow * TILE_SIZE + TILE_SIZE / 2.0 - offset
        val endX = toColumn * TILE_SIZE + TILE_SIZE / 2.0 - offset
        val endY = toRow * TILE_SIZE + TILE_SIZE / 2.0 - offset
        var shownFrame = -1
        fxLayer.play(spark, durationMs) { progress ->
            spark.x = snapToArtPixel(startX + (endX - startX) * progress)
            spark.y = snapToArtPixel(startY + (endY - startY) * progress)
            if (authoredFrames != null) {
                val frame = FxViews.steps(progress, 2 * FxViews.SPREAD_FRAMES - 1) % FxViews.SPREAD_FRAMES
                if (frame != shownFrame) {
                    shownFrame = frame
                    spark.showFrame(authoredFrames[frame])
                }
            }
        }
    }

    /**
     * Shows a unit that is walking through [row], [column], without touching the unit that stands on that tile.
     * Every hop shows the next walk frame, so the steps follow the movement instead of the clock.
     */
    fun showWalker(
        unitType: String,
        row: Int,
        column: Int,
        walkerId: String = unitType,
    ) {
        val frames = sprites.unitFrames(unitType, SpriteRegistry.WALK)
        val step = walkerSteps[walkerId]?.plus(1) ?: 0
        walkerSteps[walkerId] = step
        val walker =
            walkers.getOrPut(walkerId) {
                Container().also { container ->
                    container.name = FxViews.WALKER_NAME
                    container.image(frames.first()) {
                        scale = PIXEL_SCALE.toDouble()
                        smoothing = false
                    }
                    fxLayer.addChild(container)
                }
            }
        (walker as Container).firstChild.let { it as Image }.bitmap = frames[step % frames.size].slice()
        walker.x = column * TILE_SIZE.toDouble()
        walker.y = row * TILE_SIZE.toDouble()
    }

    fun hideWalker(unitId: String) {
        walkers.remove(unitId)?.removeFromParent()
        walkerSteps.remove(unitId)
    }

    /** True when the unit sprite of [unitId] is currently drawn as a walker. */
    fun isWalking(unitId: String): Boolean = unitId in walkers

    /** Removes every unit sprite and overlay. The presenter then draws the living units again. */
    fun removeAllBattleUnits() {
        battlefieldGrid.children.forEach { view ->
            val tileButton = view as UIButton
            tileButton.findViewByName(BATTLE_UNIT)?.removeFromParent()
            tileButton.findViewByName(UNIT_OVERLAY)?.removeFromParent()
        }
    }

    private fun tileY(row: Int): Double = row * TILE_SIZE.toDouble()

    private fun placeOnTile(
        view: View,
        row: Int,
        column: Int,
        artX: Int = 0,
        artY: Int = 0,
    ) {
        view.x = column * TILE_SIZE + artX * PIXEL_SCALE.toDouble()
        view.y = row * TILE_SIZE + artY * PIXEL_SCALE.toDouble()
    }

    private fun UIButton.addImage(
        bitmap: Bitmap,
        viewName: String,
    ): Image {
        val button = this
        return button.image(bitmap) {
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
