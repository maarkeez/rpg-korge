package com.mkz.rpg.screen.adapters.presentation.selection.domain

import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface SelectionEvent : DomainEvent {
    object Deselected : SelectionEvent

    data class BattleUnitSelected(
        val tile: TileDto,
        val battleUnitId: String,
        val walkingRange: Set<TileDto>,
    ) : SelectionEvent

    data class AbilitySelected(
        val casterTile: TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val castGroupsWhereCanCast: List<CastGroupDto>,
    ) : SelectionEvent

    /** An ability selected only to read what it does, with no cast range. */
    data class AbilityInfoDisplayed(
        val casterTile: TileDto,
        val battleUnitId: String,
        val abilityId: String,
    ) : SelectionEvent

    data class AbilityDeselected(
        val abilityId: String,
    ) : SelectionEvent

    data class CastOnCasterOrVacantTilePreviewed(
        val casterBattleUnitId: String,
        val abilityId: String,
        val castGroup: CastGroupDto,
    ) : SelectionEvent

    data class CastOnBattleUnitsPreviewed(
        val casterBattleUnitId: String,
        val abilityId: String,
        val castGroup: CastGroupDto,
        // By row and column.
        val recipientBattleUnitIds: List<String>,
    ) : SelectionEvent
}
