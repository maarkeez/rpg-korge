package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import korlibs.image.color.RGBA

// Records what the presenters ask the battlefield view to display, instead of drawing it.
class FakeBattlefieldView : BattlefieldView(BattleSceneResourcesLoader()) {
    data class BattleUnitAt(
        val row: Int,
        val column: Int,
        val unitId: String,
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
    val potentialMovements = mutableListOf<PositionDto>()
    val potentialCasts = mutableListOf<PositionDto>()
    val tileSelections = mutableListOf<PositionDto>()
    var resetTilesCount = 0
        private set
    val tilesNotDimmed = mutableListOf<Set<PositionDto>>()
    val removedBattleUnits = mutableListOf<PositionDto>()
    val movedBattleUnits = mutableListOf<MovedBattleUnit>()
    val tints = mutableListOf<RGBA>()
    val flashedBattleUnits = mutableListOf<BattleUnitAt>()

    // The loot last displayed on each tile.
    val displayedLoot = mutableMapOf<PositionDto, List<String>>()

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
    ) {
        movedBattleUnits += MovedBattleUnit(fromRow, fromColumn, path, unitId)
    }

    override fun displayLoot(
        row: Int,
        column: Int,
        lootableItemIds: List<String>,
    ) {
        displayedLoot[PositionDto(row, column)] = lootableItemIds
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
