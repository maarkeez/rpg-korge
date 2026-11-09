package com.mkz.rpg.screen.adapters.presentation.selection.domain

import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import kotlin.random.Random

object SelectionMother {
    fun nothingSelected(): NothingSelected = NothingSelected(emptySet())

    fun battleUnitSelected(
        tile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        walkingRange: Set<TileDto> = setOf(tile(0, 1), tile(1, 0)),
    ): BattleUnitSelected =
        BattleUnitSelected(
            tile = tile,
            battleUnitId = battleUnitId,
            walkingRange = walkingRange,
            events = emptySet(),
        )

    fun abilitySelected(
        casterTile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        abilityId: String = abilityId(),
        walkingRange: Set<TileDto> = setOf(tile(0, 1)),
        castGroupsWhereCanCast: List<CastGroupDto> = listOf(castGroup(tile(1, 1))),
    ): AbilitySelected =
        AbilitySelected(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            walkingRange = walkingRange,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            events = emptySet(),
        )

    fun abilityInfoDisplayed(
        casterTile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        abilityId: String = abilityId(),
        walkingRange: Set<TileDto> = setOf(tile(0, 1)),
    ): AbilityInfoDisplayed =
        AbilityInfoDisplayed(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            walkingRange = walkingRange,
            events = emptySet(),
        )

    fun castPreviewed(
        casterTile: TileDto = tile(0, 0),
        battleUnitId: String = battleUnitId(),
        abilityId: String = abilityId(),
        walkingRange: Set<TileDto> = setOf(tile(0, 1)),
        castGroupsWhereCanCast: List<CastGroupDto> = listOf(castGroup(tile(1, 1))),
        castGroup: CastGroupDto = castGroup(tile(1, 1)),
        recipientBattleUnitIds: List<String> = emptyList(),
    ): CastPreviewed =
        CastPreviewed(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            walkingRange = walkingRange,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            castGroup = castGroup,
            recipientBattleUnitIds = recipientBattleUnitIds,
            events = emptySet(),
        )

    fun tile(
        row: Int,
        column: Int,
    ) = TileDto(row = row, column = column)

    fun castGroup(vararg tiles: TileDto) = CastGroupDto(tiles = tiles.toList())

    fun battleUnitId() = "battle-unit-${Random.nextInt(1, 10000)}"

    fun abilityId() = "ability-${Random.nextInt(1, 10000)}"
}
