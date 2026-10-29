package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Duration

// Records what the presenters ask the battlefield view to display, instead of drawing it.
class FakeBattlefieldView : BattlefieldView(BattleSceneResourcesLoader()) {
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
    val tints = mutableListOf<RGBA>()
    val flashedBattleUnits = mutableListOf<BattleUnitAt>()

    // The health flashed on each tile, in order.
    val flashedHealth = mutableListOf<Pair<TilePosition, BattlefieldHealthBarPreviewView.PreviewOption>>()

    // When set, every flash lasts until it is completed, e.g.: to keep the timeline playing while it flashes.
    var flashing: CompletableDeferred<Unit>? = null

    // The loot last displayed on each tile.
    val displayedLoot = mutableMapOf<TilePosition, List<String>>()
    val pickedUpLoot = mutableListOf<PickedUpLoot>()
    val updatedTerrains = mutableListOf<UpdatedTerrain>()

    // The image of each terrain, so presenters can display it elsewhere (e.g.: the map builder palette).
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

    override fun displayLoot(
        row: Int,
        column: Int,
        lootableItemIds: List<String>,
    ) {
        displayedLoot[TilePosition(row, column)] = lootableItemIds
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

    override suspend fun pickUpLoot(
        row: Int,
        column: Int,
        lootableItemId: String,
        remainingLootableItemIds: List<String>,
    ) {
        pickedUpLoot += PickedUpLoot(row, column, lootableItemId)
        displayedLoot[TilePosition(row, column)] = remainingLootableItemIds
    }

    override suspend fun tint(color: RGBA) {
        tints += color
    }

    override suspend fun flashBattleUnit(
        row: Int,
        column: Int,
        unitId: String,
    ) {
        flashedBattleUnits += BattleUnitAt(row, column, unitId)
        flashing?.await()
    }

    override suspend fun flashHealth(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        flashedHealth += TilePosition(row, column) to previewOption
        flashing?.await()
    }
}
