package com.mkz.rpg.battlefieldHud.domain

import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import kotlin.random.Random

object BattlefieldHudMother {
    fun idle(): Idle = Idle(emptySet())

    fun displayMovementRange(
        tile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        tilesWhereCanBeMoved: Set<TileDto> = setOf(tile(0, 1), tile(1, 0)),
    ): DisplayMovementRange =
        DisplayMovementRange(
            tile = tile,
            battleUnitId = battleUnitId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events = emptySet(),
        )

    fun displayAbilityCastRange(
        casterTile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        abilityId: String = abilityId(),
        tilesWhereCanBeMoved: Set<TileDto> = setOf(tile(0, 1)),
        tilesWhereCanCast: Set<TileDto> = setOf(tile(1, 1)),
    ): DisplayAbilityCastRange =
        DisplayAbilityCastRange(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            tilesWhereCanCast = tilesWhereCanCast,
            events = emptySet(),
        )

    fun displayAbilityCastPreview(
        casterTile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        abilityId: String = abilityId(),
        tilesWhereCanBeMoved: Set<TileDto> = setOf(tile(0, 1)),
        tilesWhereCanCast: Set<TileDto> = setOf(tile(1, 1)),
        castTile: TileDto = tile(1, 1),
        enemyBattleUnitId: String? = null,
    ): DisplayAbilityCastPreview =
        DisplayAbilityCastPreview(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            tilesWhereCanCast = tilesWhereCanCast,
            castTile = castTile,
            enemyBattleUnitId = enemyBattleUnitId,
            events = emptySet(),
        )

    fun tile(
        row: Int,
        column: Int,
    ) = TileDto(row = row, column = column)

    fun battleUnitId() = "battle-unit-${Random.nextInt(1, 10000)}"

    fun abilityId() = "ability-${Random.nextInt(1, 10000)}"
}
