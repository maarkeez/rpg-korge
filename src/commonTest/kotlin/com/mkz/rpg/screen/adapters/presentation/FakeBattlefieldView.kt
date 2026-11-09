package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Duration

// Records what the presenters ask the battlefield view to display, instead of drawing it.
class FakeBattlefieldView : BattlefieldView(GameSceneResourcesLoader()) {
    data class BattleUnitAt(
        val row: Int,
        val column: Int,
        val unitId: String,
    )

    data class NpcAt(
        val row: Int,
        val column: Int,
        val npcId: String,
        val unitId: String? = null,
    )

    data class BuildingAt(
        val row: Int,
        val column: Int,
        val buildingId: String,
    )

    data class UpdatedTerrain(
        val battlefield: BattlefieldTerrain,
        val positions: Set<TilePosition>,
    )

    data class PickedUpLoot(
        val row: Int,
        val column: Int,
        val lootableItemId: String,
    )

    data class MovedBattleUnit(
        val fromRow: Int,
        val fromColumn: Int,
        val path: List<TilePosition>,
        val unitId: String,
    )

    data class SwappedBattleUnits(
        val leader: BattleUnitAt,
        val member: BattleUnitAt,
    )

    var delegate: Delegate? = null
        private set
    val displayedBattlefields = mutableListOf<BattlefieldTerrain>()
    val displayedBattleUnits = mutableListOf<BattleUnitAt>()
    val displayedNpcs = mutableListOf<NpcAt>()
    val displayedQuestMarkers = mutableMapOf<TilePosition, QuestMarkerView.Sign>()
    val displayedBuildings = mutableListOf<BuildingAt>()
    val potentialMovements = mutableListOf<TilePosition>()
    val potentialCasts = mutableListOf<TilePosition>()
    val tileSelections = mutableListOf<TilePosition>()

    // The health preview last displayed on each tile.
    val healthPreviews = mutableMapOf<TilePosition, BattlefieldHealthBarPreviewView.PreviewOption>()
    var resetTilesCount = 0
        private set
    val tilesNotDimmed = mutableListOf<Set<TilePosition>>()
    val scrolledToTiles = mutableListOf<TilePosition>()
    val removedBattleUnits = mutableListOf<TilePosition>()
    val removedNpcs = mutableListOf<TilePosition>()
    val movedBattleUnits = mutableListOf<MovedBattleUnit>()
    val moveStepDurations = mutableListOf<Duration>()

    // How each move scrolls the battlefield to follow the battle unit.
    val moveFollowings = mutableListOf<BattlefieldView.Following>()
    val swappedBattleUnits = mutableListOf<SwappedBattleUnits>()

    // The tiles the battlefield started trailing the battle unit on, and how many times it stopped trailing.
    val trailedTiles = mutableListOf<TilePosition>()
    var stopTrailingCount = 0
        private set
    var removeHealthBlinksCount = 0
        private set
    val tints = mutableListOf<RGBA>()
    val blinkedBattleUnits = mutableListOf<BattleUnitAt>()

    // The health blinked on each tile, in order.
    val blinkedHealth = mutableListOf<Pair<TilePosition, BattlefieldHealthBarPreviewView.PreviewOption>>()

    // When set, every blink lasts until it is completed, e.g.: to keep the timeline playing while it blinks.
    var blinking: CompletableDeferred<Unit>? = null

    // The loot last displayed on each tile.
    val displayedDroppedItems = mutableMapOf<TilePosition, List<String>>()
    val pickedUpLoot = mutableListOf<PickedUpLoot>()
    val updatedTerrains = mutableListOf<UpdatedTerrain>()

    // The image of each terrain, so presenters can display it elsewhere (e.g.: the world builder palette).
    val terrainBitmaps = mutableMapOf<String, Bitmap>()

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun displayBattlefield(battlefield: BattlefieldTerrain) {
        displayedBattlefields += battlefield
    }

    override fun displayBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        displayedBattleUnits += BattleUnitAt(row, column, unitId)
    }

    override fun displayQuestMarker(
        row: Int,
        column: Int,
        sign: QuestMarkerView.Sign,
    ) {
        displayedQuestMarkers[TilePosition(row, column)] = sign
    }

    override fun hideQuestMarker(
        row: Int,
        column: Int,
    ) {
        displayedQuestMarkers.remove(TilePosition(row, column))
    }

    override fun displayNpc(
        row: Int,
        column: Int,
        npcId: String,
        unitId: String?,
    ) {
        displayedNpcs += NpcAt(row, column, npcId, unitId)
    }

    override fun displayBuilding(
        row: Int,
        column: Int,
        buildingId: String,
    ) {
        displayedBuildings += BuildingAt(row, column, buildingId)
    }

    override fun displayPotentialMovement(
        row: Int,
        column: Int,
    ) {
        potentialMovements += TilePosition(row, column)
    }

    override fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        potentialCasts += TilePosition(row, column)
    }

    override fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        tileSelections += TilePosition(row, column)
    }

    override fun displayHealthPreview(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        healthPreviews[TilePosition(row, column)] = previewOption
    }

    override fun resetTiles() {
        resetTilesCount++
    }

    override fun dimTilesExcept(tiles: Set<TilePosition>) {
        tilesNotDimmed += tiles
    }

    override suspend fun scrollToTile(
        row: Int,
        column: Int,
    ) {
        scrolledToTiles += TilePosition(row, column)
    }

    override fun trail(
        row: Int,
        column: Int,
    ) {
        trailedTiles += TilePosition(row, column)
    }

    override fun stopTrailing() {
        stopTrailingCount++
    }

    override fun removeHealthBlinks() {
        removeHealthBlinksCount++
    }

    override fun removeNpc(
        row: Int,
        column: Int,
    ) {
        removedNpcs += TilePosition(row, column)
    }

    override fun removeBattleUnit(
        row: Int,
        column: Int,
    ) {
        removedBattleUnits += TilePosition(row, column)
    }

    override suspend fun moveBattleUnit(
        fromRow: Int,
        fromColumn: Int,
        path: List<TilePosition>,
        unitId: String,
        stepDuration: Duration,
        following: BattlefieldView.Following,
    ) {
        movedBattleUnits += MovedBattleUnit(fromRow, fromColumn, path, unitId)
        moveStepDurations += stepDuration
        moveFollowings += following
    }

    override suspend fun swapBattleUnits(
        leaderRow: Int,
        leaderColumn: Int,
        leaderUnitId: String,
        memberRow: Int,
        memberColumn: Int,
        memberUnitId: String,
    ) {
        swappedBattleUnits += SwappedBattleUnits(BattleUnitAt(leaderRow, leaderColumn, leaderUnitId), BattleUnitAt(memberRow, memberColumn, memberUnitId))
    }

    override fun displayDroppedItems(
        row: Int,
        column: Int,
        lootableItemIds: List<String>,
    ) {
        displayedDroppedItems[TilePosition(row, column)] = lootableItemIds
    }

    override fun updateTerrain(
        battlefield: BattlefieldTerrain,
        positions: Collection<TilePosition>,
    ) {
        updatedTerrains += UpdatedTerrain(battlefield, positions.toSet())
    }

    override fun terrainTileBitmap(
        terrainId: String,
        terrainTransition: TerrainTransition?,
    ): Bitmap = terrainBitmaps.getOrPut(terrainId) { Bitmap32(1, 1) }

    override suspend fun lootDroppedItem(
        row: Int,
        column: Int,
        lootableItemId: String,
        remainingLootableItemIds: List<String>,
    ) {
        pickedUpLoot += PickedUpLoot(row, column, lootableItemId)
        displayedDroppedItems[TilePosition(row, column)] = remainingLootableItemIds
    }

    override suspend fun tint(color: RGBA) {
        tints += color
    }

    override suspend fun blinkBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        blinkedBattleUnits += BattleUnitAt(row, column, unitId)
        blinking?.await()
    }

    override suspend fun blinkHealth(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        blinkedHealth += TilePosition(row, column) to previewOption
        blinking?.await()
    }
}
