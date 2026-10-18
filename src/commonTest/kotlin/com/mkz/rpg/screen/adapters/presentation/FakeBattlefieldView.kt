package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
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
    )

    data class UpdatedTerrain(
        val battlefield: Battlefield.Dto,
        val positions: Set<PositionDto>,
    )

    data class PickedUpLoot(
        val row: Int,
        val column: Int,
        val lootableItemId: String,
    )

    data class MovedBattleUnit(
        val fromRow: Int,
        val fromColumn: Int,
        val path: List<PositionDto>,
        val unitId: String,
    )

    var delegate: Delegate? = null
        private set
    val displayedBattlefields = mutableListOf<Battlefield.Dto>()
    val displayedBattleUnits = mutableListOf<BattleUnitAt>()
    val displayedNpcs = mutableListOf<NpcAt>()
    val potentialMovements = mutableListOf<PositionDto>()
    val potentialCasts = mutableListOf<PositionDto>()
    val tileSelections = mutableListOf<PositionDto>()

    // The health preview last displayed on each tile.
    val healthPreviews = mutableMapOf<PositionDto, BattlefieldHealthBarPreviewView.PreviewOption>()
    var resetTilesCount = 0
        private set
    val tilesNotDimmed = mutableListOf<Set<PositionDto>>()
    val removedBattleUnits = mutableListOf<PositionDto>()
    val movedBattleUnits = mutableListOf<MovedBattleUnit>()
    val moveStepDurations = mutableListOf<Duration>()

    // The moves that do not scroll the battlefield to follow the battle unit (e.g.: a CPU battle unit wandering).
    val unfollowedMoves = mutableListOf<MovedBattleUnit>()
    val tints = mutableListOf<RGBA>()
    val flashedBattleUnits = mutableListOf<BattleUnitAt>()

    // The loot last displayed on each tile.
    val displayedLoot = mutableMapOf<PositionDto, List<String>>()
    val pickedUpLoot = mutableListOf<PickedUpLoot>()
    val updatedTerrains = mutableListOf<UpdatedTerrain>()

    // The image of each terrain, so presenters can display it elsewhere (e.g.: the map builder palette).
    val terrainBitmaps = mutableMapOf<String, Bitmap>()

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun displayBattlefield(battlefield: Battlefield.Dto) {
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
    ) {
        displayedNpcs += NpcAt(row, column, npcId)
    }

    override fun displayPotentialMovement(
        row: Int,
        column: Int,
    ) {
        potentialMovements += PositionDto(row, column)
    }

    override fun displayPotentialCast(
        row: Int,
        column: Int,
    ) {
        potentialCasts += PositionDto(row, column)
    }

    override fun displayTileSelection(
        row: Int,
        column: Int,
    ) {
        tileSelections += PositionDto(row, column)
    }

    override fun displayHealthPreview(
        row: Int,
        column: Int,
        previewOption: BattlefieldHealthBarPreviewView.PreviewOption,
    ) {
        healthPreviews[PositionDto(row, column)] = previewOption
    }

    override fun resetTiles() {
        resetTilesCount++
    }

    override fun dimTilesExcept(tiles: Set<PositionDto>) {
        tilesNotDimmed += tiles
    }

    override fun removeBattleUnit(
        row: Int,
        column: Int,
    ) {
        removedBattleUnits += PositionDto(row, column)
    }

    override suspend fun moveBattleUnit(
        fromRow: Int,
        fromColumn: Int,
        path: List<PositionDto>,
        unitId: String,
        stepDuration: Duration,
        followsTiles: Boolean,
    ) {
        movedBattleUnits += MovedBattleUnit(fromRow, fromColumn, path, unitId)
        moveStepDurations += stepDuration
        if (!followsTiles) unfollowedMoves += MovedBattleUnit(fromRow, fromColumn, path, unitId)
    }

    override fun displayLoot(
        row: Int,
        column: Int,
        lootableItemIds: List<String>,
    ) {
        displayedLoot[PositionDto(row, column)] = lootableItemIds
    }

    override fun updateTerrain(
        battlefield: Battlefield.Dto,
        positions: Collection<PositionDto>,
    ) {
        updatedTerrains += UpdatedTerrain(battlefield, positions.toSet())
    }

    override fun terrainTileBitmap(
        terrainId: String,
        terrainTransition: Battlefield.Dto.TerrainTransitionDto?,
    ): Bitmap = terrainBitmaps.getOrPut(terrainId) { Bitmap32(1, 1) }

    override suspend fun pickUpLoot(
        row: Int,
        column: Int,
        lootableItemId: String,
        remainingLootableItemIds: List<String>,
    ) {
        pickedUpLoot += PickedUpLoot(row, column, lootableItemId)
        displayedLoot[PositionDto(row, column)] = remainingLootableItemIds
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
    }
}
